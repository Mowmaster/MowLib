package com.mowmaster.mowlib.transport;

import com.mojang.serialization.Codec;
import com.mowmaster.mowlib.client.MowLibClientParticles;
import com.mowmaster.mowlib.filter.FilterDirection;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.transfer.RangedResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.fluid.FluidStacksResourceHandler;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;

// Fluids. The storage is host.getTankCount(FLUIDS) tanks (one unless the host says more), each holding one fluid,
// up to host.getCapacity(FLUIDS) mB.
public class FluidTransportType extends TransportType<FluidResource>
{
    public FluidTransportType()
    {
        super(ChatFormatting.BLUE);
    }

    @Override
    public TransportStorage<FluidResource> createStorage(TransportStorageHost host)
    {
        return new Storage(host, this);
    }

    @Override
    public <T extends BlockEntity> void registerBlockCapability(RegisterCapabilitiesEvent event, BlockEntityType<T> blockEntityType,
                                                                BiFunction<T, Direction, ResourceHandler<FluidResource>> storage)
    {
        event.registerBlockEntity(Capabilities.Fluid.BLOCK, blockEntityType, storage::apply);
    }

    // ---- Other things' storage ----

    @Override
    public @Nullable ResourceHandler<FluidResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return level.getCapability(Capabilities.Fluid.BLOCK, pos, side);
    }

    @Override
    public @Nullable ResourceHandler<FluidResource> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return Capabilities.Fluid.ENTITY.getCapability(entity, side);
    }

    // Buckets, bottles, tanks...
    @Override
    public @Nullable ResourceHandler<FluidResource> getItemHandler(ItemAccess access)
    {
        return access.getCapability(Capabilities.Fluid.ITEM);
    }

    // ---- In the world ----

    // Places a source block (a bucket's worth) where there's room for one, from the first tank whose fluid can go
    // there. Water boils away in the nether, like emptying a bucket there.
    @Override
    public boolean dropInWorld(ServerLevel level, BlockPos pos, ResourceHandler<FluidResource> from, int amount, @Nullable ServerPlayer cause)
    {
        if (amount < FluidType.BUCKET_VOLUME) return false;
        BlockState there = level.getBlockState(pos);
        for (int index = 0; index < from.size(); index++)
        {
            FluidResource stored = from.getResource(index);
            if (stored.isEmpty()) continue;
            FluidStack bucket = stored.toStack(FluidType.BUCKET_VOLUME);
            if (!there.isAir() && !there.canBeReplaced(stored.getFluid())) continue;

            FluidType fluidType = stored.getFluid().getFluidType();
            FluidState toPlace = fluidType.getStateForPlacement(level, pos, bucket);
            if (!fluidType.canBePlacedInLevel(level, pos, bucket)) continue;

            // extract without an index: a bucket of this fluid from any of the tanks holding it
            try (Transaction transaction = Transaction.openRoot())
            {
                if (from.extract(stored, FluidType.BUCKET_VOLUME, transaction) < FluidType.BUCKET_VOLUME) continue;
                transaction.commit();
            }
            if (fluidType.isVaporizedOnPlacement(level, pos, bucket))
            {
                fluidType.onVaporize(cause, level, pos, bucket);
            }
            else
            {
                level.setBlock(pos, toPlace.createLegacyBlock(), Block.UPDATE_ALL_IMMEDIATE);
            }
            return true;
        }
        return false;
    }

    // ---- Saving ----

    @Override
    public Codec<FluidResource> getResourceCodec()
    {
        return FluidResource.CODEC;
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, FluidResource> getResourceStreamCodec()
    {
        return FluidResource.STREAM_CODEC;
    }

    // "Water: 16000mB"
    @Override
    public Component describeAmount(FluidResource resource, int amount)
    {
        return Component.empty().append(resource.getHoverName()).append(": " + amountText(amount));
    }

    // Millibuckets (1000 mB = a bucket)
    @Override
    public String amountText(long amount)
    {
        return amount + " mB";
    }

    // ---- Filters ----

    // Fluid filters are set from an inventory of fluid containers (buckets, tanks...): each stands for its fluid
    @Override
    public boolean usesFilterList()
    {
        return true;
    }

    @Override
    public ItemStack filterEntryAt(Level level, BlockPos pos, Direction face)
    {
        // The crosshair passes through fluids and lands on the block behind, so also look at the space in front of
        // the face (the water above a riverbed). getBucket: the fluid's bucket item (EMPTY_BUCKET... for none).
        for (BlockPos at : new BlockPos[]{pos, pos.relative(face)})
        {
            FluidState fluid = level.getFluidState(at);
            if (fluid.isEmpty()) continue;
            ItemStack bucket = new ItemStack(fluid.getType().getBucket());
            if (resourceFromFilterEntry(bucket) != null) return bucket;
        }
        return ItemStack.EMPTY;
    }

    @Override
    public @Nullable FluidResource resourceFromFilterEntry(ItemStack entry)
    {
        FluidStack contained = FluidUtil.getFirstStackContained(entry);
        return contained.isEmpty() ? null : FluidResource.of(contained);
    }

    @Override
    public boolean isSameKind(FluidResource first, FluidResource second)
    {
        return first.getFluid().isSame(second.getFluid());
    }

    @Override
    public @Nullable Identifier getRegistryName(FluidResource resource)
    {
        return BuiltInRegistries.FLUID.getKey(resource.getFluid());
    }

    // host.getTankCount(FLUIDS) tanks (at least 1), with the host's filter applied. Each tank's capacity is read from
    // the host each time; call resize when the number of tanks changes.
    public static class Storage extends FluidStacksResourceHandler implements TransportStorage<FluidResource>
    {
        private final TransportStorageHost host;
        private final FluidTransportType type;

        public Storage(TransportStorageHost host, FluidTransportType type)
        {
            super(Math.max(1, host.getTankCount(type)), Math.max(0, host.getCapacity(type)));
            this.host = host;
            this.type = type;
        }

        // One tank's capacity
        public int getCapacity()
        {
            return Math.max(0, host.getCapacity(type));
        }

        // How many tanks it should have now (at least 1)
        public int wantedSize()
        {
            return Math.max(1, host.getTankCount(type));
        }

        // Changes the number of tanks to wantedSize (same as ItemTransportType.Storage#resize). The fluid in tanks
        // that no longer exist is returned, for the holder to keep somewhere; with shrink = false it only ever grows
        // (when loading, where nothing can be dropped yet).
        public List<FluidStack> resize(boolean shrink)
        {
            int size = wantedSize();
            if (size == stacks.size() || (!shrink && size < stacks.size())) return List.of();
            List<FluidStack> overflow = new ArrayList<>();
            NonNullList<FluidStack> resized = NonNullList.withSize(size, FluidStack.EMPTY);
            for (int index = 0; index < stacks.size(); index++)
            {
                if (index < size) resized.set(index, stacks.get(index));
                else if (!stacks.get(index).isEmpty()) overflow.add(stacks.get(index));
            }
            setStacks(resized);
            if (!overflow.isEmpty()) host.onStorageChanged(type);
            return overflow;
        }

        // One tank as a handler of its own (NeoForge's RangedResourceHandler: a view of one index), for code that
        // only looks at index 0 or should only touch the tank clicked
        public ResourceHandler<FluidResource> tank(int index)
        {
            return RangedResourceHandler.ofSingleIndex(this, index);
        }

        // The first tank holding at least amount of this fluid, or null if no single tank does
        public @Nullable ResourceHandler<FluidResource> tankHolding(FluidResource fluid, int amount)
        {
            for (int index = 0; index < size(); index++)
            {
                if (getResource(index).equals(fluid) && getAmountAsLong(index) >= amount) return tank(index);
            }
            return null;
        }

        @Override
        public boolean isValid(int index, FluidResource resource)
        {
            return host.filterAllows(type, resource, FilterDirection.INSERT);
        }

        @Override
        protected int getCapacity(int index, FluidResource resource)
        {
            return resource.isEmpty() ? getCapacity() : host.filterLimit(type, resource, getCapacity());
        }

        @Override
        public int extract(int index, FluidResource resource, int amount, TransactionContext transaction)
        {
            if (!host.filterAllows(type, resource, FilterDirection.EXTRACT)) return 0;
            return super.extract(index, resource, amount, transaction);
        }

        @Override
        protected void onContentsChanged(int index, FluidStack previousContents)
        {
            host.onStorageChanged(type);
        }

        // "Water: 1000 mB / 16000 mB" (TransportType#describeStored), one per tank that holds something
        @Override
        public @Nullable Component describe()
        {
            MutableComponent message = null;
            for (int index = 0; index < size(); index++)
            {
                FluidResource stored = getResource(index);
                if (stored.isEmpty()) continue;
                Component tank = type.describeStored(stored, getAmountAsLong(index), getCapacity());
                if (message == null) message = Component.empty().append(tank);
                else message.append(", ").append(tank);
            }
            return message;
        }
    }

    // Bubbles in the fluid's colour. Fluid colours are only known on the client, so the work is in a client class.
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable FluidResource resource, RandomSource random)
    {
        if (resource == null || resource.isEmpty()) super.addTransferParticles(level, from, to, resource, random);
        else MowLibClientParticles.fluid(level, from, to, resource, random);
    }
}
