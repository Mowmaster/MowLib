package com.mowmaster.mowlib.client;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

// The paper look for screens (the reading HUD's sprite and ink), shared by menus built on it: a paper background,
// ink text, slot frames on paper, faded "ghost" items for empty slots, and icon toggles (faded while off).
// Colours are 0xAARRGGBB. Slot positions follow SlotGrid (an 18-pixel frame; the item 1 pixel in).
public final class PaperGui
{
    private PaperGui() {}

    // textures/gui/sprites/reading_paper.png, nine-sliced by its .png.mcmeta (any size works)
    public static final Identifier PAPER = MowLibReferences.id("reading_paper");
    // Ink: the reading HUD's dark brown, and a faded one
    public static final int INK = 0xFF3B2A1A;
    public static final int FADED_INK = 0x993B2A1A;
    // Slots on paper: a tan border, a slightly darker cream inside
    public static final int SLOT_BORDER = 0xFFA08A5E;
    public static final int SLOT_FILL = 0xFFDCCCA0;
    // Fading things out: in a slot (the slot's colour) and straight on the paper (the paper's colour)
    public static final int SLOT_FADE = 0xB0DCCCA0;
    public static final int PAPER_FADE = 0xB0EDE0BB;

    public static void paper(GuiGraphicsExtractor graphics, int x, int y, int width, int height)
    {
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, x, y, width, height);
    }

    // Text without a drop shadow (ink has none); centred on x
    public static void ink(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color)
    {
        graphics.text(font, text, x, y, color, false);
    }

    public static void inkCentred(GuiGraphicsExtractor graphics, Font font, Component text, int x, int y, int color)
    {
        graphics.text(font, text, x - font.width(text) / 2, y, color, false);
    }

    // A slot frame at (x, y) = the frame's top-left (a menu Slot's x/y minus 1)
    public static void slot(GuiGraphicsExtractor graphics, int x, int y)
    {
        SlotGrid.slot(graphics, x, y, SLOT_BORDER, SLOT_FILL);
    }

    // An item drawn faded in a slot frame: what goes in an empty slot
    public static void ghost(GuiGraphicsExtractor graphics, ItemStack shown, int x, int y)
    {
        graphics.item(shown, x + 1, y + 1);
        SlotGrid.fill(graphics, x, y, SLOT_FADE);
    }

    // A 16x16 icon (any texture, by its full path) at the item spot of the frame at (x, y), faded on the paper while
    // off. No frame: on paper, the icon alone reads as a button.
    public static void iconToggle(GuiGraphicsExtractor graphics, Identifier texture, int x, int y, boolean on)
    {
        graphics.blit(RenderPipelines.GUI_TEXTURED, texture, x + 1, y + 1, 0, 0, 16, 16, 16, 16);
        if (!on) SlotGrid.fill(graphics, x, y, PAPER_FADE);
    }
}
