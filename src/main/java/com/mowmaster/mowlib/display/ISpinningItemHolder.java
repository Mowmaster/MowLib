package com.mowmaster.mowlib.display;

import net.minecraft.world.item.ItemStack;

// A block entity that shows an item spinning above it (a book's open page, a display stand...). Implement it on the
// BlockEntity, then register MowLib's renderer for its type in your client class:
//
//     event.registerBlockEntityRenderer(MyBlockEntities.MY_TYPE.get(), SpinningItemRenderer::new);
//
// The values are read on the client every frame, so they must come from data the client has (synced through the
// block entity's update packet or its block state).
// This interface lives in common code (block entities load on servers too); the renderer is client/SpinningItemRenderer.
// (Generalised from Bibliomania's BookRenderer.)
public interface ISpinningItemHolder
{
    // The item to show; an empty stack shows nothing
    ItemStack getDisplayedItem();

    // Height of the item's centre above the bottom of the block, in blocks
    default double getDisplayHeight()
    {
        return 1.0;
    }

    // Size, 1 = the size of a dropped item
    default float getDisplayScale()
    {
        return 1.0f;
    }

    // Spin speed in degrees per tick (2 = 40 degrees a second); 0 doesn't spin
    default float getSpinSpeed()
    {
        return 2.0f;
    }
}
