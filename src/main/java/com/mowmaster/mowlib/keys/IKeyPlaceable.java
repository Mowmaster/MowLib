package com.mowmaster.mowlib.keys;

import net.minecraft.world.item.ItemStack;

// A block item the place key (MowLibKeyMappings.PLACE, default G) puts down as its block where the player looks,
// skipping whatever its right-click does instead (a book that places other blocks, a bucket-like item...). Only for
// BlockItems; the normal placing rules apply (BlockItem#place).
public interface IKeyPlaceable
{
    // Whether this stack may be placed by the key right now (default: always)
    default boolean canPlaceByKey(ItemStack stack)
    {
        return true;
    }
}
