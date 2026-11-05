package com.mowmaster.mowlib.paging;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

// The one way MowLib words pages, so the tooltip, the message over the hotbar and the reading HUD all say the same:
//   page(2, 8)                 "Page 3/8"
//   stack(12, bread)           "12 Bread"          (empty: "Empty")
//   line(contents, 2)          "Page 3/8: 12 Bread" or "Page 3/8: Empty"
// Pages are 0-based in code and shown 1-based. Lang keys: page.mowlib.page, page.mowlib.contents, page.mowlib.stack,
// page.mowlib.empty.
public final class PageText
{
    private PageText() {}

    private static final String PREFIX = "page." + MowLibReferences.MODID + ".";

    // "Page 3/8"
    public static MutableComponent page(int page, int pages)
    {
        return Component.translatable(PREFIX + "page", page + 1, pages);
    }

    // "12 Bread"
    public static MutableComponent stack(ItemResource resource, long amount)
    {
        return Component.translatable(PREFIX + "stack", amount, resource.getHoverName());
    }

    // "12 Bread", or "Empty" for an empty page
    public static MutableComponent stack(ResourceStack<ItemResource> stack)
    {
        return stack.isEmpty() ? Component.translatable(PREFIX + "empty") : stack(stack.resource(), stack.amount());
    }

    // "12 Bread" for a normal item stack (e.g. one shown in a slot)
    public static MutableComponent stack(ItemStack stack)
    {
        return stack.isEmpty() ? Component.translatable(PREFIX + "empty") : stack(ItemResource.of(stack), stack.getCount());
    }

    // "Page 3/8: 12 Bread" or "Page 3/8: Empty"
    public static MutableComponent line(PagedContents contents, int page)
    {
        return Component.translatable(PREFIX + "contents", page + 1, contents.pages(), stack(contents.stack(page)));
    }

    // The same for the selected page
    public static MutableComponent selectedLine(PagedContents contents)
    {
        return line(contents, contents.selectedPage());
    }
}
