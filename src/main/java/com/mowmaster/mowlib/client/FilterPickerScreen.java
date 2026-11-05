package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.FilterPicker;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.List;

// The Blank Filter's picker menu: every filter (FilterPicker.choices), each on a small label (the reading HUD's
// sprite), centred on the screen. Up to MowLibClientConfig.PICKER_RING_MAX of them (8 by default) go in a ring; more go
// in a grid 9 across. The hovered filter's name is on a piece of paper under them. Clicking a filter swaps one Blank
// Filter for it (FilterPicker.Pick, checked by the server) and closes the menu; Escape closes it.
// Opened by the activation key while holding a Blank Filter (ClientActivations, registered in MowLibClient).
// 26.x screens draw by "extracting" what to draw each frame (extractRenderState, given a GuiGraphicsExtractor) and get
// clicks as a MouseButtonEvent (its x(), y() and button()).
public class FilterPickerScreen extends Screen
{
    // GUI sprites (textures/gui/sprites), nine-sliced by their .png.mcmeta, shared with the reading HUD
    private static final Identifier PAPER = MowLibReferences.id("reading_paper");
    private static final Identifier LABEL = MowLibReferences.id("reading_label");
    // The reading HUD's dark brown ink (0xAARRGGBB)
    private static final int INK = 0xFF3B2A1A;
    // A filter's tile, and the bigger one under the mouse
    private static final int TILE = 22;
    private static final int TILE_HOVERED = 26;
    // Space between tiles, in the ring and the grid
    private static final int GAP = 6;
    private static final int GRID_COLUMNS = 9;
    private static final int PAPER_PADDING = 8;
    // Between the tiles and the name paper under them
    private static final int TEXT_GAP = 8;

    private final InteractionHand hand;
    private final List<Item> choices;
    // Each tile's centre, and the bottom edge of all the tiles; worked out in init (which also runs when the window is
    // resized, since the screen's width and height change)
    private int[] tileX = new int[0];
    private int[] tileY = new int[0];
    private int tilesBottom;
    // The tile under the mouse when last drawn, or -1
    private int hovered = -1;

    public FilterPickerScreen(InteractionHand hand)
    {
        super(Component.translatable("screen." + MowLibReferences.MODID + ".filter_picker"));
        this.hand = hand;
        this.choices = FilterPicker.choices();
    }

    // ---- Layout ----

    @Override
    protected void init()
    {
        super.init();
        int count = choices.size();
        tileX = new int[count];
        tileY = new int[count];
        if (count <= MowLibClientConfig.PICKER_RING_MAX.get()) layoutRing(count);
        else layoutGrid(count);
    }

    // Evenly round a ring in the middle of the screen, the first at the top, going clockwise. The ring is big enough
    // that the tiles don't overlap (its length is 2 x pi x radius).
    private void layoutRing(int count)
    {
        int radius = Math.max(40, (int) Math.ceil(count * (TILE + GAP) / (2 * Math.PI)));
        int centerX = width / 2, centerY = height / 2;
        for (int index = 0; index < count; index++)
        {
            double angle = -Math.PI / 2 + 2 * Math.PI * index / count;
            tileX[index] = centerX + (int) Math.round(Math.cos(angle) * radius);
            tileY[index] = centerY + (int) Math.round(Math.sin(angle) * radius);
        }
        tilesBottom = centerY + radius + TILE / 2;
    }

    // Rows of 9 (fewer if there are fewer filters), the whole grid centred on the screen. The last row starts at the left.
    private void layoutGrid(int count)
    {
        int step = TILE + GAP;
        int columns = Math.min(GRID_COLUMNS, count);
        int rows = (count + GRID_COLUMNS - 1) / GRID_COLUMNS;
        int left = width / 2 - (columns * step - GAP) / 2;
        int top = height / 2 - (rows * step - GAP) / 2;
        for (int index = 0; index < count; index++)
        {
            tileX[index] = left + (index % GRID_COLUMNS) * step + TILE / 2;
            tileY[index] = top + (index / GRID_COLUMNS) * step + TILE / 2;
        }
        tilesBottom = top + rows * step - GAP;
    }

    // The filter under the point, or -1
    private int tileAt(double x, double y)
    {
        for (int index = 0; index < tileX.length; index++)
        {
            if (Math.abs(x - tileX[index]) <= TILE / 2.0 && Math.abs(y - tileY[index]) <= TILE / 2.0) return index;
        }
        return -1;
    }

    // ---- Drawing (every frame) ----

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        hovered = tileAt(mouseX, mouseY);

        // Each filter on a label, the hovered one bigger
        for (int index = 0; index < tileX.length; index++)
        {
            int size = index == hovered ? TILE_HOVERED : TILE;
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LABEL, tileX[index] - size / 2, tileY[index] - size / 2, size, size);
            // Items are drawn 16 x 16 from their top-left corner
            graphics.item(new ItemStack(choices.get(index)), tileX[index] - 8, tileY[index] - 8);
        }

        // Under the tiles, on paper: the hovered filter's name, or what to do. Under them, so a long name never runs
        // across the tiles.
        String text = hovered >= 0 ? new ItemStack(choices.get(hovered)).getHoverName().getString() : title.getString();
        int paperWidth = Math.max(64, font.width(text) + 2 * PAPER_PADDING);
        int paperHeight = 9 + 2 * PAPER_PADDING;
        int paperTop = tilesBottom + TEXT_GAP;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, width / 2 - paperWidth / 2, paperTop, paperWidth, paperHeight);
        // false: no drop shadow (ink doesn't have one)
        graphics.text(font, text, width / 2 - font.width(text) / 2, paperTop + PAPER_PADDING, INK, false);
    }

    // ---- Clicking ----

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        // Since 26.3 mouse buttons use SDL's numbers (left is 1, where it used to be 0), so compare with the
        // InputConstants constant rather than a raw number. That raw 0 is why clicking did nothing before.
        if (event.button() == InputConstants.MOUSE_BUTTON_LEFT)
        {
            int index = tileAt(event.x(), event.y());
            // The tile last drawn as hovered, if the click's position didn't land on one
            if (index < 0) index = hovered;
            if (index >= 0)
            {
                Identifier id = BuiltInRegistries.ITEM.getKey(choices.get(index));
                ClientPacketDistributor.sendToServer(new FilterPicker.Pick(hand == InteractionHand.OFF_HAND, id));
                onClose();
                return true;
            }
        }
        return super.mouseClicked(event, doubleClick);
    }
}
