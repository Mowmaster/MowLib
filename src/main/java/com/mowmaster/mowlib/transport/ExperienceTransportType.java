package com.mowmaster.mowlib.transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.util.MowLibParticles;
import com.mowmaster.mowlib.util.MowLibXpUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.ItemCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiFunction;

// Experience, stored as points. NeoForge has no experience capability, so MowLib makes two, both named
// "mowlib:experience_handler" and both holding EXPERIENCE: BLOCK for block entities (tanks, machines) and ITEM for
// items that hold experience (other mods register their items for it with RegisterCapabilitiesEvent#registerItem).
public class ExperienceTransportType extends TransportType<AmountResource>
{
    public static final AmountResource EXPERIENCE = new AmountResource(MowLibReferences.id("experience"));

    public static final BlockCapability<ResourceHandler<AmountResource>, Direction> BLOCK =
            BlockCapability.createSided(MowLibReferences.id("experience_handler"), ResourceHandler.asClass());

    // ItemAccess as the context: the same as NeoForge's own item capabilities (Capabilities.Fluid.ITEM...)
    public static final ItemCapability<ResourceHandler<AmountResource>, ItemAccess> ITEM =
            ItemCapability.create(MowLibReferences.id("experience_handler"), ResourceHandler.asClass(), ItemAccess.class);

    public ExperienceTransportType()
    {
        super(ChatFormatting.GREEN);
    }

    @Override
    public TransportStorage<AmountResource> createStorage(TransportStorageHost host)
    {
        return new Storage(host, this);
    }

    @Override
    public <T extends BlockEntity> void registerBlockCapability(RegisterCapabilitiesEvent event, BlockEntityType<T> blockEntityType,
                                                                BiFunction<T, Direction, ResourceHandler<AmountResource>> storage)
    {
        event.registerBlockEntity(BLOCK, blockEntityType, storage::apply);
    }

    // ---- Other things' storage: anything offering mowlib:experience_handler ----

    @Override
    public @Nullable ResourceHandler<AmountResource> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return level.getCapability(BLOCK, pos, side);
    }

    @Override
    public @Nullable ResourceHandler<AmountResource> getItemHandler(ItemAccess access)
    {
        return access.getCapability(ITEM);
    }

    // ---- In the world ----

    // An experience orb worth up to amount
    @Override
    public boolean dropInWorld(ServerLevel level, BlockPos pos, ResourceHandler<AmountResource> from, int amount, @Nullable ServerPlayer cause)
    {
        int extracted;
        try (Transaction transaction = Transaction.openRoot())
        {
            extracted = from.extract(EXPERIENCE, amount, transaction);
            if (extracted <= 0) return false;
            transaction.commit();
        }
        Vec3 centre = Vec3.atCenterOf(pos);
        level.addFreshEntity(new ExperienceOrb(level, centre.x, centre.y, centre.z, extracted));
        return true;
    }

    // Experience orbs (whole orbs only: an orb's value can't be changed), and bottles o' enchanting, which are worth
    // 3-10 points each and leave glass bottles behind. Then items offering mowlib:experience_handler.
    @Override
    public boolean absorbEntity(ServerLevel level, Entity entity, ResourceHandler<AmountResource> into, int amount)
    {
        if (entity instanceof ExperienceOrb orb)
        {
            int value = orb.getValue();
            if (value <= 0) return false;
            try (Transaction transaction = Transaction.openRoot())
            {
                if (into.insert(EXPERIENCE, value, transaction) < value) return false;
                transaction.commit();
            }
            orb.discard();
            return true;
        }
        if (entity instanceof ItemEntity itemEntity && itemEntity.getItem().is(Items.EXPERIENCE_BOTTLE))
        {
            int count = itemEntity.getItem().getCount();
            int value = 0;
            for (int bottle = 0; bottle < count; bottle++) value += 3 + level.getRandom().nextInt(8);
            try (Transaction transaction = Transaction.openRoot())
            {
                if (into.insert(EXPERIENCE, value, transaction) < value) return false;
                transaction.commit();
            }
            itemEntity.setItem(new ItemStack(Items.GLASS_BOTTLE, count));
            return true;
        }
        return super.absorbEntity(level, entity, into, amount);
    }

    // The player's own experience (their real total, see MowLibXpUtils), then items they carry
    @Override
    public int pullFromPlayer(Player player, ResourceHandler<AmountResource> into, int amount)
    {
        int wanted = Math.min(amount, MowLibXpUtils.getPlayerExperience(player));
        if (wanted > 0)
        {
            int inserted;
            try (Transaction transaction = Transaction.openRoot())
            {
                inserted = into.insert(EXPERIENCE, wanted, transaction);
                if (inserted > 0) transaction.commit();
            }
            if (inserted > 0)
            {
                MowLibXpUtils.takePlayerExperience(player, inserted);
                return inserted;
            }
        }
        return super.pullFromPlayer(player, into, amount);
    }

    // ---- Saving: the resource is always EXPERIENCE, so nothing needs saving but the amount ----

    @Override
    public Codec<AmountResource> getResourceCodec()
    {
        return MapCodec.unitCodec(EXPERIENCE);
    }

    @Override
    public StreamCodec<RegistryFriendlyByteBuf, AmountResource> getResourceStreamCodec()
    {
        return StreamCodec.unit(EXPERIENCE);
    }

    // Capacity in points (host.getCapacity(EXPERIENCE)); left behind as experience orbs
    public static class Storage extends AmountStorage
    {
        public Storage(TransportStorageHost host, ExperienceTransportType type)
        {
            super(host, type, EXPERIENCE);
        }

        @Override
        public void dropContents(Level level, BlockPos pos)
        {
            if (amount > 0 && level instanceof ServerLevel serverLevel) ExperienceOrb.award(serverLevel, Vec3.atCenterOf(pos), amount);
            amount = 0;
        }
    }

    // Glowing green and yellow specks, like experience orbs, flying to the target
    @Override
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable AmountResource resource, RandomSource random)
    {
        MowLibParticles.addTrailParticles(level, from, to, 0x80FF20, 4, random);
        MowLibParticles.addTrailParticles(level, from, to, 0xD4FF4A, 4, random);
    }
}
