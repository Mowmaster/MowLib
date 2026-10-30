package com.mowmaster.mowlib.transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.energy.EnergyHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

// Energy (FE), as an amount-only type: every other mod's NeoForge EnergyHandler is seen as a ResourceHandler of
// ENERGY (HandlerView), and our storages are offered to cables as an EnergyHandler (EnergyView).
// Energy can't be dropped in the world (dropInWorld is the default false; Pedestals' 1.20 lightning bolt is the
// dropper's own business).
public class EnergyTransportType extends TransportType<AmountResource>
{
    public static final AmountResource ENERGY = new AmountResource(MowLibReferences.id("energy"));

    public EnergyTransportType()
    {
        super(ChatFormatting.RED);
    }

    @Override
    public TransportStorage<AmountResource> createStorage(TransportStorageHost host)
    {
        return new AmountStorage(host, this, ENERGY);
    }

    // Other mods' cables see a normal NeoForge EnergyHandler
    @Override
    public <T extends BlockEntity> void registerBlockCapability(RegisterCapabilitiesEvent event, BlockEntityType<T> blockEntityType,
                                                                BiFunction<T, Direction, ResourceHandler<AmountResource>> storage)
    {
        event.registerBlockEntity(Capabilities.Energy.BLOCK, blockEntityType, (blockEntity, side) -> {
            ResourceHandler<AmountResource> handler = storage.apply(blockEntity, side);
            return handler == null ? null : new EnergyView(handler);
        });
    }

    // ---- Other things' storage: NeoForge energy handlers, seen as resource handlers ----

    @Override
    public @Nullable ResourceHandler<AmountResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return HandlerView.of(level.getCapability(Capabilities.Energy.BLOCK, pos, side));
    }

    @Override
    public @Nullable ResourceHandler<AmountResource> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return HandlerView.of(Capabilities.Energy.ENTITY.getCapability(entity, side));
    }

    // Batteries and other charged items
    @Override
    public @Nullable ResourceHandler<AmountResource> getItemHandler(ItemAccess access)
    {
        return HandlerView.of(access.getCapability(Capabilities.Energy.ITEM));
    }

    // ---- Saving: the resource is always ENERGY, so nothing needs saving but the amount ----

    @Override
    public Codec<AmountResource> getResourceCodec()
    {
        return MapCodec.unitCodec(ENERGY);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AmountResource> getResourceStreamCodec()
    {
        return StreamCodec.unit(ENERGY);
    }

    // A storage (index 0, holding ENERGY) seen as a NeoForge EnergyHandler. A record holding what it wraps.
    public record EnergyView(ResourceHandler<AmountResource> storage) implements EnergyHandler
    {
        @Override
        public long getAmountAsLong()
        {
            return storage.getAmountAsLong(0);
        }

        @Override
        public long getCapacityAsLong()
        {
            return storage.getCapacityAsLong(0, ENERGY);
        }

        @Override
        public int insert(int amount, TransactionContext transaction)
        {
            return amount <= 0 ? 0 : storage.insert(0, ENERGY, amount, transaction);
        }

        @Override
        public int extract(int amount, TransactionContext transaction)
        {
            return amount <= 0 ? 0 : storage.extract(0, ENERGY, amount, transaction);
        }
    }

    // The other way round: another mod's EnergyHandler seen as a ResourceHandler of ENERGY, so energy can be moved
    // with the same code as items and fluids
    public record HandlerView(EnergyHandler handler) implements ResourceHandler<AmountResource>
    {
        public static @Nullable HandlerView of(@Nullable EnergyHandler handler)
        {
            return handler == null ? null : new HandlerView(handler);
        }

        @Override
        public int size()
        {
            return 1;
        }

        @Override
        public AmountResource getResource(int index)
        {
            return handler.getAmountAsLong() > 0 ? ENERGY : AmountResource.EMPTY;
        }

        @Override
        public long getAmountAsLong(int index)
        {
            return handler.getAmountAsLong();
        }

        @Override
        public long getCapacityAsLong(int index, AmountResource resource)
        {
            return handler.getCapacityAsLong();
        }

        @Override
        public boolean isValid(int index, AmountResource resource)
        {
            return ENERGY.equals(resource);
        }

        @Override
        public int insert(int index, AmountResource resource, int amount, TransactionContext transaction)
        {
            return ENERGY.equals(resource) && amount > 0 ? handler.insert(amount, transaction) : 0;
        }

        @Override
        public int extract(int index, AmountResource resource, int amount, TransactionContext transaction)
        {
            return ENERGY.equals(resource) && amount > 0 ? handler.extract(amount, transaction) : 0;
        }
    }

    // A lightning bolt: electric sparks along a zig-zag line
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable AmountResource resource, RandomSource random)
    {
        int segments = 8;
        Vec3 previous = from;
        for (int segment = 1; segment <= segments; segment++)
        {
            Vec3 next = from.lerp(to, (double) segment / segments);
            if (segment < segments) next = next.add(random.nextGaussian() * 0.15, random.nextGaussian() * 0.15, random.nextGaussian() * 0.15);
            for (int spark = 0; spark < 3; spark++)
            {
                Vec3 at = previous.lerp(next, random.nextDouble());
                level.addParticle(ParticleTypes.ELECTRIC_SPARK, at.x, at.y, at.z, 0, 0, 0);
            }
            previous = next;
        }
    }
}
