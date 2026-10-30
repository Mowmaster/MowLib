package com.mowmaster.mowlib.tint;

import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// An item that dyes with a colour it works out itself, rather than being a dye item (e.g. the Color Palette, which
// dyes with its selected dye). IColorableBlock#dyeColorOf asks it first, so it dyes blocks, works in
// ColorableItemDyeRecipe, and is never used up, just like a dye.
public interface IDyeSource
{
    // The colour (0xRRGGBB) this stack dyes with right now, or null if it can't dye (e.g. an empty palette)
    @Nullable Integer getDyeColor(ItemStack stack);
}
