package com.mowmaster.mowlib.paging;

import net.minecraft.sounds.SoundEvents;
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
public final class PagedStorageInteractions
{
    private PagedStorageInteractions() {}

    // Holding the paged item, clicking a slot
    public static boolean stackedOnOther(IPagedStorageItem item, ItemStack self, Slot slot, ClickAction clickAction, Player player)
    {
        if (self.getCount() != 1) return false;
        ItemStack other = slot.getItem();

        // An item that can't be filled by hand: let the click do what it normally would
        if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && item.allowsInsert(self))
        {
            int fits = item.insert(self, other, true);
            if (fits > 0)
            {
                // safeTake respects slots you can't take from (e.g. crafting results); store what actually came out
                ItemStack taken = slot.safeTake(other.getCount(), fits, player);
                item.insert(self, taken, false);
                playInsert(player);
            }
            else
            {
                player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
            }
            slotsChanged(player);
            return true;
        }
        if (clickAction == ClickAction.SECONDARY && other.isEmpty())
        {
            ItemStack removed = item.extractSelected(self);
            if (!removed.isEmpty())
            {
                ItemStack left = slot.safeInsert(removed);
                if (!left.isEmpty()) item.insert(self, left, false); // the slot didn't take it all: put the rest back
                playRemove(player);
            }
            slotsChanged(player);
            return true;
        }
        return false;
    }

    // Holding something (or nothing), clicking the paged item
    public static boolean otherStackedOnMe(IPagedStorageItem item, ItemStack self, ItemStack other, Slot slot,
                                           ClickAction clickAction, Player player, SlotAccess carriedItem)
    {
        if (self.getCount() != 1) return false;

        if (clickAction == ClickAction.PRIMARY && !other.isEmpty() && item.allowsInsert(self))
        {
            if (slot.allowModification(player))
            {
                int inserted = item.insert(self, other, false);
                if (inserted > 0)
                {
                    other.shrink(inserted);
                    playInsert(player);
                }
                else
                {
                    player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
                }
            }
            slotsChanged(player);
            return true;
        }
        if (clickAction == ClickAction.SECONDARY && other.isEmpty())
        {
            if (slot.allowModification(player))
            {
                ItemStack removed = item.extractSelected(self);
                if (!removed.isEmpty())
                {
                    carriedItem.set(removed);
                    playRemove(player);
                }
            }
            slotsChanged(player);
            return true;
        }
        // Anything else (e.g. left-click with an empty hand picks the item up as normal)
        return false;
    }

    // Every page, empty ones included, drawn by client/ClientPagedContentsTooltip
    public static Optional<TooltipComponent> tooltipImage(IPagedStorageItem item, ItemStack stack)
    {
        return Optional.of(new PagedContentsTooltip(item.contents(stack), item.pageCapacity(stack)));
    }

    private static void playInsert(Player player)
    {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
    }

    private static void playRemove(Player player)
    {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
    }

    // Tells the open container the item changed, so the change is sent to the client
    private static void slotsChanged(Player player)
    {
        player.containerMenu.slotsChanged(player.getInventory());
    }
}
