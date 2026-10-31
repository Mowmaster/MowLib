package com.mowmaster.mowlib.display;

import org.jetbrains.annotations.Nullable;

// A block entity that shows a mob above it. Implement it on the BlockEntity, then register MowLib's renderer for
// its type in your client class:
//
//     event.registerBlockEntityRenderer(MyBlockEntities.MY_TYPE.get(), MobDisplayBlockRenderer::new);
//
// Or, if your renderer draws other things too, use client/MobDisplayRenderer inside it instead.
// The values are read on the client every frame, so they must come from data the client has (synced through the
// block entity's update packet, e.g. with MobDisplay.CODEC). Same idea as ISpinningItemHolder.
public interface IMobDisplayHolder
{
    // The mob to show, or null for none
    @Nullable MobDisplay getMobDisplay();

    // Height of the mob's feet above the bottom of the block, in blocks
    default double getMobDisplayHeight()
    {
        return 1.0;
    }
}
