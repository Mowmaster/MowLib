package com.mowmaster.mowlib.client;

import com.mowmaster.mowlib.MowLibIds;
import com.mowmaster.mowlib.paging.PagedContents;
import com.mowmaster.mowlib.paging.PagedContentsTooltip;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

import java.util.List;

// Draws a paged item's tooltip picture (PagedContentsTooltip): a line saying which page is selected and what's on
// it, a line with how full the item is, then a grid with one slot per page (empty pages included), the selected page
// outlined in white. Registered for every mod by MowLibClient. (Moved from Bibliomania's ClientBookTooltip.)
// In 26.x tooltips "extract" what to draw (extractImage) into a GuiGraphicsExtractor, which the game draws later.
public class ClientPagedContentsTooltip implements ClientTooltipComponent
{
    private static final int SLOT = 18;          // slot size in pixels, the same as inventory slots
    private static final int COLUMNS = 9;        // like a row of the inventory
    private static final int LINE_HEIGHT = 10;
    private static final int TEXT_LINES = 2;
    private static final int GAP = 2;            // between the text and the grid

    // Colours are 0xAARRGGBB (alpha first)
    private static final int SLOT_BORDER = 0xFF373737;
    private static final int SLOT_FILL = 0xFF8B8B8B;
    private static final int SELECTED = 0xFFFFFFFF;
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

    private int rows()
    {
        // Rounds up: 10 pages in rows of 9 is 2 rows
        return (contents.pages() + COLUMNS - 1) / COLUMNS;
    }

    private int selectedPage()
    {
        return contents.selectedPage();
    }

    @Override
    public int getHeight(Font font)
    {
        return TEXT_LINES * LINE_HEIGHT + GAP + rows() * SLOT + GAP;
    }

    @Override
    public int getWidth(Font font)
    {
        int width = columns() * SLOT;
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
        List<ResourceStack<ItemResource>> stacks = contents.stacks();
        for (int page = 0; page < contents.pages(); page++)
        {
            int slotX = x + (page % COLUMNS) * SLOT;
            int slotY = gridTop + (page / COLUMNS) * SLOT;

            // Slot: a dark square with a lighter one inside
            graphics.fill(slotX, slotY, slotX + SLOT, slotY + SLOT, SLOT_BORDER);
            graphics.fill(slotX + 1, slotY + 1, slotX + SLOT - 1, slotY + SLOT - 1, SLOT_FILL);
            if (page == selectedPage()) outline(graphics, slotX, slotY);

            if (page < stacks.size() && !stacks.get(page).isEmpty())
            {
                ResourceStack<ItemResource> stack = stacks.get(page);
                // Drawn as a single item with the page's amount as the count text, since a page can hold more
                // than one normal stack of something (e.g. 32 ender pearls)
                ItemStack shown = stack.resource().toStack(1);
                graphics.item(shown, slotX + 1, slotY + 1);
                graphics.itemDecorations(font, shown, slotX + 1, slotY + 1, stack.amount() > 1 ? String.valueOf(stack.amount()) : null);
            }
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

    // "Page 3/12: 24 Cobblestone" or "Page 3/12: Empty"
    private String pageLine()
    {
        int page = selectedPage();
        List<ResourceStack<ItemResource>> stacks = contents.stacks();
        Component what = page < stacks.size() && !stacks.get(page).isEmpty()
                ? Component.literal(stacks.get(page).amount() + " ").append(stacks.get(page).resource().getHoverName())
                : Component.translatable("tooltip." + MowLibIds.MODID + ".paged.empty_page");
        return Component.translatable("tooltip." + MowLibIds.MODID + ".paged.page",
                page + 1, contents.pages(), what).getString();
    }

    // "Holds 40 / 256"
    private String fullnessLine()
    {
        return Component.translatable("tooltip." + MowLibIds.MODID + ".paged.fullness",
                contents.totalAmount(), (long) pageCapacity * contents.pages()).getString();
    }
}
