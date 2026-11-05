package com.mowmaster.mowlib.client;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.palette.ColorPaletteContents;
import com.mowmaster.mowlib.palette.ColorPaletteTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;

// Draws a Color Palette's tooltip picture (ColorPaletteTooltip): a line naming the selected dye, then every dye in a
// grid (in its sorted order), the selected one outlined in white. Vanilla's bundle tooltip only shows a few items;
// this shows them all. Registered by MowLibClient. Slots drawn by SlotGrid, like ClientPagedContentsTooltip.
// In 26.x tooltips "extract" what to draw (extractImage) into a GuiGraphicsExtractor, which the game draws later.
public class ClientColorPaletteTooltip implements ClientTooltipComponent
{
    private static final int COLUMNS = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int GAP = 2;            // between the text and the grid

    // Colours are 0xAARRGGBB (alpha first)
    private static final int TEXT = 0xFFFFFFFF;

    private final ColorPaletteContents contents;

    public ClientColorPaletteTooltip(ColorPaletteTooltip tooltip)
    {
        this.contents = tooltip.contents();
    }

    private int columns()
    {
        return Math.min(COLUMNS, contents.size());
    }

    @Override
    public int getHeight(Font font)
    {
        return LINE_HEIGHT + (contents.isEmpty() ? 0 : GAP + SlotGrid.rows(contents.size(), COLUMNS) * SlotGrid.SLOT + GAP);
    }

    @Override
    public int getWidth(Font font)
    {
        return Math.max(columns() * SlotGrid.SLOT, font.width(selectedLine()));
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics)
    {
        graphics.text(font, selectedLine(), x, y, TEXT, true);
        if (contents.isEmpty()) return;

        int gridTop = y + LINE_HEIGHT + GAP;
        int selected = contents.selectedIndex();
        for (int index = 0; index < contents.size(); index++)
        {
            int slotX = SlotGrid.slotX(x, index, COLUMNS);
            int slotY = SlotGrid.slotY(gridTop, index, COLUMNS, 0);
            SlotGrid.slot(graphics, slotX, slotY, SlotGrid.TOOLTIP_BORDER, SlotGrid.TOOLTIP_FILL);
            if (index == selected) SlotGrid.outline(graphics, slotX, slotY, SlotGrid.SELECTED);
            SlotGrid.item(graphics, font, contents.dyes().get(index).create(), slotX, slotY, 1);
        }
    }

    // "Selected: Red Dye" or "No dyes"
    private String selectedLine()
    {
        if (contents.isEmpty()) return Component.translatable("tooltip." + MowLibReferences.MODID + ".palette.empty").getString();
        return Component.translatable("tooltip." + MowLibReferences.MODID + ".palette.selected",
                contents.selectedStack().getHoverName()).getString();
    }
}
