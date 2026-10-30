package com.mowmaster.mowlib.client;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.palette.ColorPaletteContents;
import com.mowmaster.mowlib.palette.ColorPaletteTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

// Draws a Color Palette's tooltip picture (ColorPaletteTooltip): a line naming the selected dye, then every dye in a
// grid (in its sorted order), the selected one outlined in white. Vanilla's bundle tooltip only shows a few items;
// this shows them all. Registered by MowLibClient. Same drawing style as ClientPagedContentsTooltip.
// In 26.x tooltips "extract" what to draw (extractImage) into a GuiGraphicsExtractor, which the game draws later.
public class ClientColorPaletteTooltip implements ClientTooltipComponent
{
    private static final int SLOT = 18;          // slot size in pixels, the same as inventory slots
    private static final int COLUMNS = 8;
    private static final int LINE_HEIGHT = 10;
    private static final int GAP = 2;            // between the text and the grid

    // Colours are 0xAARRGGBB (alpha first)
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SELECTED = 0xFFFFFFFF;
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

    private int rows()
    {
        // Rounds up: 10 dyes in rows of 8 is 2 rows
        return (contents.size() + COLUMNS - 1) / COLUMNS;
    }

    @Override
    public int getHeight(Font font)
    {
        return LINE_HEIGHT + (contents.isEmpty() ? 0 : GAP + rows() * SLOT + GAP);
    }

    @Override
    public int getWidth(Font font)
    {
        return Math.max(columns() * SLOT, font.width(selectedLine()));
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
            int slotX = x + (index % COLUMNS) * SLOT;
            int slotY = gridTop + (index / COLUMNS) * SLOT;

            // Slot: a dark square with a lighter one inside
            graphics.fill(slotX, slotY, slotX + SLOT, slotY + SLOT, SLOT_BORDER);
            graphics.fill(slotX + 1, slotY + 1, slotX + SLOT - 1, slotY + SLOT - 1, SLOT_FILL);
            if (index == selected) outline(graphics, slotX, slotY);

            ItemStack dye = contents.dyes().get(index).create();
            graphics.item(dye, slotX + 1, slotY + 1);
        }
    }

    // A 1-pixel white frame just inside the slot
    private static void outline(GuiGraphicsExtractor graphics, int x, int y)
    {
        graphics.fill(x, y, x + SLOT, y + 1, SELECTED);
        graphics.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, SELECTED);
        graphics.fill(x, y, x + 1, y + SLOT, SELECTED);
        graphics.fill(x + SLOT - 1, y, x + SLOT, y + SLOT, SELECTED);
    }

    // "Selected: Red Dye" or "No dyes"
    private String selectedLine()
    {
        if (contents.isEmpty()) return Component.translatable("tooltip." + MowLibReferences.MODID + ".palette.empty").getString();
        return Component.translatable("tooltip." + MowLibReferences.MODID + ".palette.selected",
                contents.selectedStack().getHoverName()).getString();
    }
}
