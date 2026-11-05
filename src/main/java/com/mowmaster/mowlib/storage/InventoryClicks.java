package com.mowmaster.mowlib.storage;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
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
//
// Every bundle-style item in MowLib goes through here: paged storage items (PagedStorageInteractions), the Color
// Palette, and any mod's IInventoryUsable.
public final class InventoryClicks
{
    private InventoryClicks() {}

    // The usable item on the cursor, clicked onto the item in a slot. An empty slot: placed as normal, unless the item
    // uses empty slots (IInventoryUsable#usedOnEmptySlots).
    public static boolean stackedOnOther(IInventoryUsable item, ItemStack self, Slot slot, ClickAction action, Player player)
    {
        ItemStack other = slot.getItem();
        if (self.getCount() != 1 || !slot.allowModification(player)) return false;
        if (other.isEmpty() && !item.usedOnEmptySlots(self)) return false;
        InventoryClick click = InventoryClick.slot(slot);
        if (!item.usedWith(self, other, action, player, click)) return false;
        click.changed();
        slotsChanged(player);
        return true;
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
            slotsChanged(player);
            return true;
        }
        if (!other.isEmpty() && item.refusesOtherClicks(self))
        {
            playFail(player);
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

    // ---- Sounds shared by bundle-style items ----

    // Something went in (vanilla's bundle sound, slightly varied pitch)
    public static void playInsert(Player player)
    {
        player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
    }

    // Something came out
    public static void playRemove(Player player)
    {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
    }

    // Refused (full, not allowed...)
    public static void playFail(Player player)
    {
        player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
    }

    // Tells the open container the items changed, so the change is sent to the client
    private static void slotsChanged(Player player)
    {
        player.containerMenu.slotsChanged(player.getInventory());
    }
}
