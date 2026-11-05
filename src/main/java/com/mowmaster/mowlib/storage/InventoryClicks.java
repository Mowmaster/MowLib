package com.mowmaster.mowlib.storage;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

// The inventory clicks of an IInventoryUsable item. The item overrides two Item methods and calls in here:
//
//     @Override public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player)
//     { return InventoryClicks.stackedOnOther(this, self, slot, action, player); }
//     @Override public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action,
//                                                       Player player, SlotAccess carried)
//     { return InventoryClicks.otherStackedOnMe(this, self, other, slot, action, player, carried); }
public final class InventoryClicks
{
    private InventoryClicks() {}

    // The usable item on the cursor, clicked onto the item in a slot (an empty slot: placed as normal)
    public static boolean stackedOnOther(IInventoryUsable item, ItemStack self, Slot slot, ClickAction action, Player player)
    {
        ItemStack other = slot.getItem();
        if (self.getCount() != 1 || other.isEmpty() || !slot.allowModification(player)) return false;
        InventoryClick click = InventoryClick.slot(slot);
        boolean handled = item.usedWith(self, other, action, player, click);
        if (handled) click.changed();
        return handled;
    }

    // Something (or nothing) on the cursor, clicked onto the usable item in a slot
    public static boolean otherStackedOnMe(IInventoryUsable item, ItemStack self, ItemStack other, Slot slot, ClickAction action,
                                           Player player, SlotAccess carried)
    {
        if (self.getCount() != 1) return false;
        // A left-click with an empty cursor picks the item up as normal
        if (action == ClickAction.PRIMARY && other.isEmpty()) return false;
        if (slot.allowModification(player) && item.usedWith(self, other, action, player, InventoryClick.cursor(slot, carried)))
        {
            player.containerMenu.slotsChanged(player.getInventory());
            return true;
        }
        if (!other.isEmpty() && item.refusesOtherClicks(self))
        {
            player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
            return true;
        }
        return false;
    }

    // The item in a hand used on the usable item as if clicked onto it in an inventory, first as a left-click, then as
    // a right-click: for a block standing in for the item (a lectern showing it, a placed version...). Server.
    public static boolean useHeld(IInventoryUsable item, ItemStack self, Player player, InteractionHand hand)
    {
        ItemStack held = player.getItemInHand(hand);
        if (held.isEmpty()) return false;
        InventoryClick click = InventoryClick.hand(hand);
        return item.usedWith(self, held, ClickAction.PRIMARY, player, click) || item.usedWith(self, held, ClickAction.SECONDARY, player, click);
    }
}
