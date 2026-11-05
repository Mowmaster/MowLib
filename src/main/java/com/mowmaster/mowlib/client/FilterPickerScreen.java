package com.mowmaster.mowlib.client;

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

// The Blank Filter's radial menu: every filter (FilterPicker.choices) in a ring round the middle of the screen, each on
// a small label, with the hovered one's name on a piece of paper in the centre (the reading HUD's sprites). Clicking a
// filter swaps one Blank Filter for it (FilterPicker.Pick, checked by the server) and closes the menu; Escape closes it.
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
    private static final int GAP = 6;
    private static final int PAPER_PADDING = 8;

    private final InteractionHand hand;
    private final List<Item> choices;

    public FilterPickerScreen(InteractionHand hand)
    {
        super(Component.translatable("screen." + MowLibReferences.MODID + ".filter_picker"));
        this.hand = hand;
        this.choices = FilterPicker.choices();
    }

    // ---- Layout: the ring ----

    // Big enough that the tiles round the ring don't overlap (the ring's length is 2 x pi x radius)
    private int radius()
    {
        return Math.max(48, (int) Math.ceil(choices.size() * (TILE + GAP) / (2 * Math.PI)));
    }

    // The centre of filter `index`'s tile: evenly round the ring, the first at the top, going clockwise
    private int tileX(int index)
    {
        return width / 2 + (int) Math.round(Math.cos(angle(index)) * radius());
    }

    private int tileY(int index)
    {
        return height / 2 + (int) Math.round(Math.sin(angle(index)) * radius());
    }

    private double angle(int index)
    {
        return -Math.PI / 2 + 2 * Math.PI * index / choices.size();
    }

    // The filter under the mouse, or -1
    private int hoveredIndex(double mouseX, double mouseY)
    {
        for (int index = 0; index < choices.size(); index++)
        {
            if (Math.abs(mouseX - tileX(index)) <= TILE / 2.0 && Math.abs(mouseY - tileY(index)) <= TILE / 2.0) return index;
        }
        return -1;
    }

    // ---- Drawing (every frame) ----

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick)
    {
        super.extractRenderState(graphics, mouseX, mouseY, partialTick);
        int hovered = hoveredIndex(mouseX, mouseY);

        // The paper in the middle: the hovered filter's name, or what to do
        String text = hovered >= 0 ? new ItemStack(choices.get(hovered)).getHoverName().getString() : title.getString();
        int paperWidth = Math.max(64, font.width(text) + 2 * PAPER_PADDING);
        int paperHeight = 9 + 2 * PAPER_PADDING;
        int centerX = width / 2, centerY = height / 2;
        graphics.blitSprite(RenderPipelines.GUI_TEXTURED, PAPER, centerX - paperWidth / 2, centerY - paperHeight / 2, paperWidth, paperHeight);
        // false: no drop shadow (ink doesn't have one)
        graphics.text(font, text, centerX - font.width(text) / 2, centerY - 4, INK, false);

        // Each filter on a label, the hovered one bigger
        for (int index = 0; index < choices.size(); index++)
        {
            int size = index == hovered ? TILE_HOVERED : TILE;
            int x = tileX(index), y = tileY(index);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, LABEL, x - size / 2, y - size / 2, size, size);
            // Items are drawn 16 x 16 from their top-left corner
            graphics.item(new ItemStack(choices.get(index)), x - 8, y - 8);
        }
    }

    // ---- Clicking ----

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick)
    {
        // Button 0 is the left mouse button
        if (event.button() == 0)
        {
            int index = hoveredIndex(event.x(), event.y());
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
