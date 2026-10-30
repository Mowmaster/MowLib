package com.mowmaster.mowlib.transport;

import com.mojang.serialization.Codec;
import com.mowmaster.mowlib.network.TransferParticlesPayload;
import com.mowmaster.mowlib.storage.ItemEntityAccess;
import com.mowmaster.mowlib.util.MowLibParticles;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.function.BiFunction;

// One kind of thing that can be stored and moved: items, fluids, energy, experience, or anything another mod
// registers (e.g. dust). This class only knows the KIND of thing: how to store it, how to reach it in blocks,
// entities and items, how to save it, filter it, drop it and show it. How much a machine moves or holds is up to
// the machine (Pedestals keeps that in PedestalTransportRules).
//
// To add a type from another mod: extend this class and register an instance in a DeferredRegister made with
// DeferredRegister.create(TransportTypes.KEY, "yourmodid"). <R> is the type's NeoForge Resource class
// (ItemResource, FluidResource, ...). Amount-only types (energy-like) can use AmountResource and AmountStorage.
public abstract class TransportType<R extends Resource>
{
    private final ChatFormatting color;

    protected TransportType(ChatFormatting color)
    {
        this.color = color;
    }

    // ---- Storage ----

    // A storage for this type, sized by host.getCapacity(this). Usually called once per type in a block entity's
    // constructor (see TransportTypes.REGISTRY to make one of every type).
    public abstract TransportStorage<R> createStorage(TransportStorageHost host);

    // true if clients need the contents (e.g. a renderer shows items). Other types only save, which is cheaper.
    public boolean syncsToClient()
    {
        return false;
    }

    // Moves up to amount from one handler to another. Storages enforce their own filters.
    public int transfer(ResourceHandler<R> from, ResourceHandler<R> to, int amount, TransactionContext transaction)
    {
        return ResourceHandlerUtil.moveStacking(from, to, resource -> true, amount, transaction);
    }

    // Lets hoppers, pipes and other mods reach a block entity's storage of this type: registers the type's block
    // capability (items, fluids, energy, mowlib:experience_handler...) for blockEntityType, answered by storage.
    // Call it from RegisterCapabilitiesEvent for every type your block entity stores. Default: not reachable.
    public <T extends BlockEntity> void registerBlockCapability(RegisterCapabilitiesEvent event, BlockEntityType<T> blockEntityType,
                                                                BiFunction<T, Direction, ResourceHandler<R>> storage)
    {
    }

    // ---- Reaching other things' storage ----

    // This type's handler in the block at pos, seen from side. null = the block has none.
    public @Nullable ResourceHandler<R> getBlockHandler(Level level, BlockPos pos, @Nullable Direction side)
    {
        return null;
    }

    // This type's handler on an entity (chest minecarts...). null = none.
    public @Nullable ResourceHandler<R> getEntityHandler(Entity entity, @Nullable Direction side)
    {
        return null;
    }

    // This type's handler inside an item (a bucket's fluid, a battery's energy...). null = none.
    // ItemAccess is NeoForge's "the item and where it lives": the handler can swap the item (full bucket -> empty bucket).
    public @Nullable ResourceHandler<R> getItemHandler(ItemAccess access)
    {
        return null;
    }

    // ---- In the world ----

    // Puts up to amount from `from` into the world at pos (items, xp orbs, a fluid source...). true if anything
    // happened. Default: this type can't be dropped.
    public boolean dropInWorld(ServerLevel level, BlockPos pos, ResourceHandler<R> from, int amount, @Nullable ServerPlayer cause)
    {
        return false;
    }

    // Takes up to amount of this type from an entity (a magnet, something touching a machine). true if anything
    // was taken. Default: an item entity's item is emptied through getItemHandler (a bucket of water gives its water
    // and stays behind as an empty bucket). Items and experience override this (item pickup, xp orbs).
    public boolean absorbEntity(ServerLevel level, Entity entity, ResourceHandler<R> into, int amount)
    {
        if (!(entity instanceof ItemEntity itemEntity) || itemEntity.getItem().isEmpty()) return false;
        ItemEntityAccess access = new ItemEntityAccess(itemEntity);
        ResourceHandler<R> handler = getItemHandler(access.access());
        if (handler == null) return false;
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = transfer(handler, into, amount, transaction);
            if (moved <= 0) return false;
            transaction.commit();
        }
        access.writeBack(level);
        return true;
    }

    // Takes up to amount of this type from a player: out of the first item in their main inventory (slots 0-35,
    // never armour or the offhand) that holds some and gives some up. Returns how much was taken.
    // Items and experience override this (the stack itself, the player's own experience).
    public int pullFromPlayer(Player player, ResourceHandler<R> into, int amount)
    {
        Inventory inventory = player.getInventory();
        for (int slot = 0; slot < Inventory.INVENTORY_SIZE; slot++)
        {
            if (inventory.getItem(slot).isEmpty()) continue;
            ResourceHandler<R> handler = getItemHandler(ItemAccess.forPlayerSlot(player, slot));
            if (handler == null) continue;
            try (Transaction transaction = Transaction.openRoot())
            {
                int moved = transfer(handler, into, amount, transaction);
                if (moved <= 0) continue;
                transaction.commit();
                return moved;
            }
        }
        return 0;
    }

    // ---- Saving (storage packets, contents in items) ----

    // How this type's resource is saved, so TransportContents can hold it. null = can't be saved in an item.
    public @Nullable Codec<R> getResourceCodec()
    {
        return null;
    }

    // The same for the network (tooltips, particles). Only used when getResourceCodec isn't null.
    public @Nullable StreamCodec<RegistryFriendlyByteBuf, R> getResourceStreamCodec()
    {
        return null;
    }

    // One line for a tooltip, e.g. "Water: 16000mB"
    public Component describeAmount(R resource, int amount)
    {
        return Component.empty().append(getName()).append(": " + amount);
    }

    // ---- Filters ----

    // Whether filters can be set up for this type by sneak-right-clicking an inventory with the filter
    public boolean usesFilterList()
    {
        return false;
    }

    // What an item in a filter's list stands for in this type (the item itself, the fluid in a bucket...). null = nothing.
    public @Nullable R resourceFromFilterEntry(ItemStack entry)
    {
        return null;
    }

    // Same thing ignoring extra data (fuzzy filters): same item, same fluid
    public boolean isSameKind(R first, R second)
    {
        return first.equals(second);
    }

    // The registry name, for the mod filter (null if it has none)
    public @Nullable Identifier getRegistryName(R resource)
    {
        return null;
    }

    // ---- Display ----

    public ChatFormatting getColor()
    {
        return color;
    }

    // The colour as RGB, for particles
    public int getRgb()
    {
        TextColor textColor = TextColor.fromLegacyFormat(color);
        return textColor != null ? textColor.getValue() : 0xFFFFFF;
    }

    public Identifier getId()
    {
        return Objects.requireNonNull(TransportTypes.REGISTRY.getKey(this), "Transport type is not registered");
    }

    // "Items", "Fluids"... from the lang key transport_type.<namespace>.<path>
    public Component getName()
    {
        return Component.translatable(getId().toLanguageKey("transport_type")).withStyle(color);
    }

    // ---- Particles ----

    // Shows some of this type moving from one place to another. Call it on the server; nearby players get a
    // TransferParticlesPayload and draw it with addTransferParticles.
    public void sendTransferParticles(ServerLevel level, Vec3 from, Vec3 to, @Nullable R resource)
    {
        TransferParticlesPayload.send(level, this, from, to, resource);
    }

    // Runs on the CLIENT. resource is what moved (null if the type has no resource stream codec).
    // Default: sparks in the type's colour flying from `from` to `to`. Override to draw your own; anything client-only
    // (textures, tints...) must be in a separate client class called from here, as this class also loads on servers.
    public void addTransferParticles(Level level, Vec3 from, Vec3 to, @Nullable R resource, RandomSource random)
    {
        MowLibParticles.addTrailParticles(level, from, to, getRgb(), 6, random);
    }
}
