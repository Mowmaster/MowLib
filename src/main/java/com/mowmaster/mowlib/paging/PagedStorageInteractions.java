package com.mowmaster.mowlib.paging;

import com.mowmaster.mowlib.storage.IInventoryUsable;
import com.mowmaster.mowlib.storage.InventoryClick;
import com.mowmaster.mowlib.storage.InventoryClicks;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;

import java.util.Optional;

// Bundle-style inventory clicks and the page tooltip for any IPagedStorageItem. An interface can't override Item's
// methods, so the item overrides these three and calls in here:
//
//     @Override public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player)
//     { return PagedStorageInteractions.stackedOnOther(this, self, slot, action, player); }
//     @Override public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action,
//                                                       Player player, SlotAccess carried)
//     { return PagedStorageInteractions.otherStackedOnMe(this, self, other, slot, action, player, carried); }
//     @Override public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
//     { return PagedStorageInteractions.tooltipImage(this, stack); }
//
// The clicks:
// - Holding the item, left-click an item in a slot: stores it.      Holding an item, left-click this item: stores it.
// - Holding the item, right-click an empty slot: takes the selected page out into it.
//   Empty-handed, right-click this item: takes the selected page out onto your cursor.
// These run on both sides (the client predicts), like vanilla's bundle. (Moved from Bibliomania's BookItem.)
// IPagedStorageItem#allowsTakeOut can refuse the take-out clicks for a page (it's in use).
//
// The guards (one item, slot may change, syncing) are InventoryClicks', shared with every MowLib bundle-style item.
// An item that is also an IInventoryUsable with clicks of its own can call usedWith from its own usedWith instead.
public final class PagedStorageInteractions
{
    private PagedStorageInteractions() {}

    // Holding the paged item, clicking a slot
    public static boolean stackedOnOther(IPagedStorageItem item, ItemStack self, Slot slot, ClickAction clickAction, Player player)
    {
        return InventoryClicks.stackedOnOther(usable(item), self, slot, clickAction, player);
    }

    // Holding something (or nothing), clicking the paged item
    public static boolean otherStackedOnMe(IPagedStorageItem item, ItemStack self, ItemStack other, Slot slot,
                                           ClickAction clickAction, Player player, SlotAccess carriedItem)
    {
        return InventoryClicks.otherStackedOnMe(usable(item), self, other, slot, clickAction, player, carriedItem);
    }

    // Every page, empty ones included, drawn by client/ClientPagedContentsTooltip
    public static Optional<TooltipComponent> tooltipImage(IPagedStorageItem item, ItemStack stack)
    {
        return Optional.of(new PagedContentsTooltip(item.contents(stack), item.pageCapacity(stack)));
    }

    // The clicks themselves (see the class comment). `other` is the item clicked with or onto; `click` says where it is.
    public static boolean usedWith(IPagedStorageItem item, ItemStack self, ItemStack other, ClickAction action, Player player, InventoryClick click)
    {
        // Left-click with/onto an item: store as much as fits. An item that can't be filled by hand: the click does
        // what it normally would.
        if (action == ClickAction.PRIMARY && !other.isEmpty())
        {
            if (!item.allowsInsert(self)) return false;
            int fits = item.insert(self, other, true);
            if (fits > 0)
            {
                // take: from a slot through safeTake (respects slots you can't take from); store what actually came
                ItemStack taken = click.take(other, fits, player);
                item.insert(self, taken, false);
                InventoryClicks.playInsert(player);
            }
            else InventoryClicks.playFail(player);
            return true;
        }
        // Right-click onto an empty slot, or with an empty cursor: the selected page comes out there
        if (action == ClickAction.SECONDARY && other.isEmpty())
        {
            if (!item.allowsTakeOut(self, item.contents(self).selectedPage()))
            {
                InventoryClicks.playFail(player);
                return true;
            }
            ItemStack removed = item.extractSelected(self);
            if (!removed.isEmpty())
            {
                ItemStack left = click.putTaken(removed, player);
                if (!left.isEmpty()) item.insert(self, left, false); // it didn't all fit there: put the rest back
                InventoryClicks.playRemove(player);
            }
            return true;
        }
        return false;
    }

    // The paged item seen as an IInventoryUsable, so InventoryClicks can run its clicks. A record holding the item.
    private static IInventoryUsable usable(IPagedStorageItem item)
    {
        return new Usable(item);
    }

    private record Usable(IPagedStorageItem item) implements IInventoryUsable
    {
        @Override
        public boolean usedWith(ItemStack self, ItemStack other, ClickAction action, Player player, InventoryClick click)
        {
            return PagedStorageInteractions.usedWith(item, self, other, action, player, click);
        }

        // Right-click an empty slot: the selected page comes out into it
        @Override
        public boolean usedOnEmptySlots(ItemStack self)
        {
            return true;
        }
    }
}
