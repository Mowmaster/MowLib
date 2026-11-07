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
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.fluid.FluidUtil;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Function;

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
        return held == null ? 0 : move(type, held, into, amount);
    }

    // The other way: fills the held item from a storage. Returns how much moved.
    public static <R extends Resource> int emptyIntoHeld(Player player, InteractionHand hand, TransportType<R> type, ResourceHandler<R> from, int amount)
    {
        ResourceHandler<R> held = type.getItemHandler(heldAccess(player, hand));
        return held == null ? 0 : move(type, from, held, amount);
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

    // ---- Reaching items in inventory slots (for inventory clicks) ----

    // Runs `action` on the item in `slot` as an ItemAccess (what NeoForge's item capabilities need, so a bucket can
    // become an empty bucket in place). For the player's own inventory this is the real slot (anything that doesn't
    // fit goes to the rest of the inventory). For other containers the item is copied into a 2-slot holder, the action
    // runs there, and the result goes back into the slot (the second slot catches anything extra, given to the player).
    // Server only (it changes things).
    public static <T> T withSlotAccess(Player player, Slot slot, Function<ItemAccess, T> action)
    {
        if (slot.container == player.getInventory())
        {
            return action.apply(ItemAccess.forPlayerSlot(player, slot.getContainerSlot()));
        }
        TwoSlotItemAccess holder = new TwoSlotItemAccess(slot.getItem());
        T result = action.apply(holder.access());
        slot.set(holder.result());
        giveToPlayer(player, holder.extra());
        return result;
    }

    // The item on the cursor (in an open container) as an ItemAccess
    public static ItemAccess cursorAccess(Player player)
    {
        return ItemAccess.forPlayerCursor(player, player.containerMenu);
    }

    // The item on the cursor (a bucket, a tank, a battery...) used on a storage, in an open container: an item holding
    // the type empties into the storage, otherwise the storage fills the item. The cursor item can change in place
    // (bucket -> water bucket); the menu sends it to the client. For one tank of several, pass that tank's own view
    // (FluidTransportType.Storage#tank). Server only. Returns how much moved.
    public static <R extends Resource> int useCursorContainer(Player player, TransportType<R> type, ResourceHandler<R> storage)
    {
        if (player.containerMenu.getCarried().isEmpty()) return 0;
        ResourceHandler<R> item = type.getItemHandler(cursorAccess(player));
        if (item == null) return 0;
        int moved = move(type, item, storage, Integer.MAX_VALUE);
        return moved > 0 ? moved : move(type, storage, item, Integer.MAX_VALUE);
    }

    // Moves up to `amount` of a type between two handlers, committed (TransportType#transfer, so the type's own rules
    // apply). Returns how much moved. The handlers must hold the type's resource: the casts are only safe then.
    @SuppressWarnings("unchecked")
    public static <R extends Resource> int move(TransportType<R> type, ResourceHandler<?> from, ResourceHandler<?> to, int amount)
    {
        try (Transaction transaction = Transaction.openRoot())
        {
            int moved = type.transfer((ResourceHandler<R>) from, (ResourceHandler<R>) to, amount, transaction);
            if (moved > 0) transaction.commit();
            return moved;
        }
    }
}
