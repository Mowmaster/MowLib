package com.mowmaster.mowlib.storage;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.item.ItemStack;

// An item that other items can be used on in an inventory, either way round, like a bundle: a book holding a mob fed
// with food or sheared with shears, a planter given seeds, a trader paid with emeralds...
// - `other` on the cursor, clicked onto this item in a slot, or this item on the cursor clicked onto `other` in a slot,
//   or (an empty cursor) a right-click on this item: usedWith(self, other, action, player, click)
// - a left-click on this item with an empty cursor always picks it up as normal
// InventoryClicks does the guards (one item, slot may change, client/server) and calls usedWith; the item overrides
// overrideStackedOnOther / overrideOtherStackedOnMe to call InventoryClicks (an interface can't override Item methods).
// usedWith runs on both sides and must answer the same on both; change things on the server only
// (player.level().isClientSide()). `click` says where `other` is, to use it up or swap it for a result.
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
}
