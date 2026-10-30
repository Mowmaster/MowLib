package com.mowmaster.mowlib.storage;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.IFilterHolder;
import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.items.tools.FilterToolItem;
import com.mowmaster.mowlib.transport.TransportType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Prediction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// Putting things into a storage block by hand and taking them out, the same way in every mod. Call these from
// your block's useItemOn (right-click) and attack (survival left-click) on the SERVER (level.isClientSide() false):
//   right-click with an item     -> insertHeld (and useFluidContainer first for buckets and tanks)
//   left-click                   -> takeToPlayer (1, or a stack when sneaking)
//   offhand filter + right-click -> insertFilter; offhand filter tool + left-click -> removeFilter
// Creative players don't "attack" blocks, they break them straight away (Block#onDestroyedByPlayer): if a
// creative left-click should take items out instead, do it there and return false while the storage isn't empty.
public final class StorageInteractions
{
    private StorageInteractions()
    {
    }

    // ---- Items ----

    // Puts as much of the held stack in as fits. Creative players keep their stack (ItemStack#consume).
    // Returns how many went in.
    public static int insertHeld(Player player, InteractionHand hand, ResourceHandler<ItemResource> into)
    {
        ItemStack stack = player.getItemInHand(hand);
        if (stack.isEmpty()) return 0;
        int inserted = insertStack(into, stack, false);
        if (inserted > 0) stack.consume(inserted, player);
        return inserted;
    }

    // How many of the stack go in (simulate = only check). The stack itself isn't changed.
    public static int insertStack(ResourceHandler<ItemResource> into, ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty()) return 0;
        // try-with-resources: the transaction is closed at the end of the block, and anything not committed is undone
        try (Transaction transaction = Transaction.openRoot())
        {
            int inserted = ResourceHandlerUtil.insertStacking(into, ItemResource.of(stack), stack.getCount(), transaction);
            if (!simulate) transaction.commit();
            return inserted;
        }
    }

    // Takes up to maxAmount (and at most one normal stack) from the last slot that has something
    // (newest first, as Pedestals has always done). EMPTY if there's nothing to take.
    public static ItemStack extractStack(ResourceHandler<ItemResource> from, int maxAmount)
    {
        for (int index = from.size() - 1; index >= 0; index--)
        {
            ItemResource resource = from.getResource(index);
            if (resource.isEmpty()) continue;
            int amount = Math.min(maxAmount, resource.getMaxStackSize());
            try (Transaction transaction = Transaction.openRoot())
            {
                int extracted = from.extract(index, resource, amount, transaction);
                if (extracted <= 0) continue;
                transaction.commit();
                return resource.toStack(extracted);
            }
        }
        return ItemStack.EMPTY;
    }

    // Left-click: 1 item, or a stack when sneaking, into the player's inventory. Returns what was taken.
    public static ItemStack takeToPlayer(Player player, ResourceHandler<ItemResource> from)
    {
        ItemStack taken = extractStack(from, player.isShiftKeyDown() ? Integer.MAX_VALUE : 1);
        giveToPlayer(player, taken);
        return taken;
    }

    // Into the inventory, or dropped at the player's feet if it's full. The client is sent the inventory change.
    public static void giveToPlayer(Player player, ItemStack stack)
    {
        if (!stack.isEmpty()) player.getInventory().placeItemBackInInventory(stack, Prediction.SERVER_ONLY);
    }

    // ---- Other types ----

    // Buckets and other fluid containers in the hand fill from / empty into the block's fluid handler (NeoForge's
    // helper does the swap in the player's hand and plays the sound). true if anything happened.
    public static boolean useFluidContainer(Player player, InteractionHand hand, Level level, BlockPos pos)
    {
        return FluidUtil.interactWithFluidHandler(player, hand, level, pos, null, null);
    }

    // The held item as an ItemAccess (for TransportType#getItemHandler): the main hand's hotbar slot or the offhand
    public static ItemAccess heldAccess(Player player, InteractionHand hand)
    {
        int slot = hand == InteractionHand.MAIN_HAND ? player.getInventory().getSelectedSlot() : Inventory.SLOT_OFFHAND;
        return ItemAccess.forPlayerSlot(player, slot);
    }

    // Empties up to amount of a type out of the held item (a battery's energy, an experience tome...) into a
    // storage. Returns how much moved.
    public static <R extends Resource> int fillFromHeld(Player player, InteractionHand hand, TransportType<R> type, ResourceHandler<R> into, int amount)
    {
        ResourceHandler<R> held = type.getItemHandler(heldAccess(player, hand));
        if (held == null) return 0;
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = type.transfer(held, into, amount, transaction);
            if (moved > 0) transaction.commit();
            return moved;
        }
    }

    // The other way: fills the held item from a storage. Returns how much moved.
    public static <R extends Resource> int emptyIntoHeld(Player player, InteractionHand hand, TransportType<R> type, ResourceHandler<R> from, int amount)
    {
        ResourceHandler<R> held = type.getItemHandler(heldAccess(player, hand));
        if (held == null) return 0;
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = type.transfer(from, held, amount, transaction);
            if (moved > 0) transaction.commit();
            return moved;
        }
    }

    // ---- Filters ----

    // A filter in the offhand goes in (one per holder). true if this was a filter click (handled, even if the
    // holder was full), false if the offhand isn't a filter.
    public static boolean insertFilter(Player player, IFilterHolder holder)
    {
        ItemStack offhand = player.getOffhandItem();
        if (!(offhand.getItem() instanceof ITransportFilter)) return false;
        holder.insertFilter(offhand);
        return true;
    }

    // The filter tool in the offhand takes the filter out (or says there isn't one). true if this was a filter
    // tool click.
    public static boolean removeFilter(Player player, IFilterHolder holder)
    {
        if (!(player.getOffhandItem().getItem() instanceof FilterToolItem)) return false;
        if (holder.hasFilter()) giveToPlayer(player, holder.removeFilter());
        else player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".tool_filter_missing"));
        return true;
    }
}
