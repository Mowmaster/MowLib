package com.mowmaster.mowlib.palette;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

// The data for a Color Palette's tooltip picture: every dye, with the selected one outlined. ColorPaletteItem
// creates it; on the client, client/ClientColorPaletteTooltip draws it (registered by MowLibClient). This half is
// common code because items, which servers load too, create it.
public record ColorPaletteTooltip(ColorPaletteContents contents) implements TooltipComponent
{
}
