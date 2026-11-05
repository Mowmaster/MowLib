package com.mowmaster.mowlib.client;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;

// Drawing grids of item slots in GUIs and HUDs: the paged item tooltip, the Color Palette tooltip, the reading HUD's
// page grid. Slots are SLOT pixels square (the same as inventory slots); an item is drawn 1 pixel in from the slot's
// corner. Colours are 0xAARRGGBB (alpha first).
public final class SlotGrid
{
    private SlotGrid() {}

    public static final int SLOT = 18;

    // Tooltip-style slots: a dark square with a lighter one inside
    public static final int TOOLTIP_BORDER = 0xFF373737;
    public static final int TOOLTIP_FILL = 0xFF8B8B8B;
    // The selected slot's ring in tooltips
    public static final int SELECTED = 0xFFFFFFFF;

    // Where slot `index` goes in a grid `columns` wide starting at (left, top), skipping `firstRow` rows (for a
    // scrolled grid): its left edge and top edge
    public static int slotX(int left, int index, int columns)
    {
        return left + (index % columns) * SLOT;
    }

    public static int slotY(int top, int index, int columns, int firstRow)
    {
        return top + (index / columns - firstRow) * SLOT;
    }

    // How many rows `count` slots take, `columns` to a row (rounds up: 10 in rows of 9 is 2)
    public static int rows(int count, int columns)
    {
        return columns <= 0 ? 0 : (count + columns - 1) / columns;
    }

    // A tooltip-style slot: `border` all over, `fill` inside it
    public static void slot(GuiGraphicsExtractor graphics, int x, int y, int border, int fill)
    {
        graphics.fill(x, y, x + SLOT, y + SLOT, border);
        fill(graphics, x, y, fill);
    }

    // Just the inside of a slot (1 pixel in from its edge)
    public static void fill(GuiGraphicsExtractor graphics, int x, int y, int color)
    {
        graphics.fill(x + 1, y + 1, x + SLOT - 1, y + SLOT - 1, color);
    }

    // A 1-pixel frame round a slot. The four strips don't overlap at the corners, so see-through colours stay even.
    public static void outline(GuiGraphicsExtractor graphics, int x, int y, int color)
    {
        graphics.fill(x, y, x + SLOT, y + 1, color);
        graphics.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, color);
        graphics.fill(x, y + 1, x + 1, y + SLOT - 1, color);
        graphics.fill(x + SLOT - 1, y + 1, x + SLOT, y + SLOT - 1, color);
    }

    // An item in a slot. amount: the number written on it when more than 1 (a page can hold more than a normal stack,
    // so the item is drawn once with the whole amount as its count); 0 or 1 writes nothing.
    public static void item(GuiGraphicsExtractor graphics, Font font, ItemStack shown, int x, int y, long amount)
    {
        if (shown.isEmpty()) return;
        graphics.item(shown, x + 1, y + 1);
        graphics.itemDecorations(font, shown, x + 1, y + 1, amount > 1 ? String.valueOf(amount) : null);
    }
}
