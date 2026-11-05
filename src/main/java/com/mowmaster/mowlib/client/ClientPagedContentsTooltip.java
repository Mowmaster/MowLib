package com.mowmaster.mowlib.client;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.paging.PageText;
import com.mowmaster.mowlib.paging.PagedContents;
import com.mowmaster.mowlib.paging.PagedContentsTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

// Draws a paged item's tooltip picture (PagedContentsTooltip): a line saying which page is selected and what's on
// it, a line with how full the item is, then a grid with one slot per page (empty pages included), the selected page
// outlined in white. Registered for every mod by MowLibClient. (Moved from Bibliomania's ClientBookTooltip.)
// In 26.x tooltips "extract" what to draw (extractImage) into a GuiGraphicsExtractor, which the game draws later.
// The page wording is PageText's and the slot drawing SlotGrid's, shared with the reading HUD.
public class ClientPagedContentsTooltip implements ClientTooltipComponent
{
    private static final int COLUMNS = 9;        // like a row of the inventory
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_LINES = 2;
    private static final int GAP = 2;            // between the text and the grid

    // Colours are 0xAARRGGBB (alpha first)
    private static final int TEXT = 0xFFFFFFFF;
    private static final int TEXT_DIM = 0xFFAAAAAA;

    private final PagedContents contents;
    private final int pageCapacity;

    public ClientPagedContentsTooltip(PagedContentsTooltip tooltip)
    {
        this.contents = tooltip.contents();
        this.pageCapacity = tooltip.pageCapacity();
    }

    private int columns()
    {
        return Math.min(COLUMNS, contents.pages());
    }

    @Override
    public int getHeight(Font font)
    {
        return TEXT_LINES * LINE_HEIGHT + GAP + SlotGrid.rows(contents.pages(), COLUMNS) * SlotGrid.SLOT + GAP;
    }

    @Override
    public int getWidth(Font font)
    {
        int width = columns() * SlotGrid.SLOT;
        width = Math.max(width, font.width(pageLine()));
        width = Math.max(width, font.width(fullnessLine()));
        return width;
    }

    @Override
    public void extractImage(Font font, int x, int y, int width, int height, GuiGraphicsExtractor graphics)
    {
        graphics.text(font, pageLine(), x, y, TEXT, true);
        graphics.text(font, fullnessLine(), x, y + LINE_HEIGHT, TEXT_DIM, true);

        int gridTop = y + TEXT_LINES * LINE_HEIGHT + GAP;
        int selected = contents.selectedPage();
        for (int page = 0; page < contents.pages(); page++)
        {
            int slotX = SlotGrid.slotX(x, page, COLUMNS);
            int slotY = SlotGrid.slotY(gridTop, page, COLUMNS, 0);
            SlotGrid.slot(graphics, slotX, slotY, SlotGrid.TOOLTIP_BORDER, SlotGrid.TOOLTIP_FILL);
            if (page == selected) SlotGrid.outline(graphics, slotX, slotY, SlotGrid.SELECTED);

            ResourceStack<ItemResource> stack = contents.stack(page);
            if (!stack.isEmpty()) SlotGrid.item(graphics, font, stack.resource().toStack(1), slotX, slotY, stack.amount());
        }
    }

    // "Page 3/12: 24 Cobblestone" or "Page 3/12: Empty"
    private String pageLine()
    {
        return PageText.selectedLine(contents).getString();
    }

    // "Holds 40 / 256"
    private String fullnessLine()
    {
        return Component.translatable("tooltip." + MowLibReferences.MODID + ".paged.fullness",
                contents.totalAmount(), (long) pageCapacity * contents.pages()).getString();
    }
}
