package com.mowmaster.mowlib.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;

// An item that other items can be used on in an inventory, either way round, like a bundle: a storage book filled
// with items, the Color Palette given dyes, a book holding a mob fed with food, a planter given seeds...
// - `other` on the cursor, clicked onto this item in a slot, or this item on the cursor clicked onto `other` in a slot,
//   or (an empty cursor) a right-click on this item: usedWith(self, other, action, player, click)
// - this item on the cursor clicked onto an EMPTY slot: usedWith too, with `other` empty, but only for items that say
//   so with usedOnEmptySlots (bundle-style "take one out into this slot")
// - a left-click on this item with an empty cursor always picks it up as normal
// InventoryClicks does the guards (one item, slot may change) and calls usedWith; the item overrides
// overrideStackedOnOther / overrideOtherStackedOnMe to call InventoryClicks (an interface can't override Item methods).
// usedWith runs on both sides and must answer the same on both. Change things either on the server only
// (player.level().isClientSide()), or the same way on both sides as vanilla's bundle does (the client then shows the
// result straight away). `click` says where `other` is: take from it, use it up, swap it for a result, or put
// something taken out of this item there.
public interface IInventoryUsable
{
    // true: handled (nothing else happens)
    boolean usedWith(ItemStack self, ItemStack other, ClickAction action, Player player, InventoryClick click);

    // true: any other click with something on the cursor onto this item is refused (fail sound) instead of doing the
    // normal thing (swapping the stacks)
    default boolean refusesOtherClicks(ItemStack self)
    {
        return false;
    }

    // true: this item on the cursor clicked onto an empty slot calls usedWith (with `other` empty) instead of being
    // put down there. Bundle-style items use it to take their selected item out into that slot on a right-click
    // (return false from usedWith for a left-click, so the item is put down as normal).
    default boolean usedOnEmptySlots(ItemStack self)
    {
        return false;
    }
}
