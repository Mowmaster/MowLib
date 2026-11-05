package com.mowmaster.mowlib.paging;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// An item with pages, one of which is selected (a storage book, a manual...). Implement it on the Item class.
// MowLib then lets players change the page without any code in the mod:
// - scrolling over the item in an inventory, or pressing the page keys (default Page Down / Page Up) over it
// - pressing the page keys while holding it (and not looking at a block with pages, see IPageHolder)
// The page lives on the stack (usually in a data component), so the item decides how it's stored.
public interface IPageable
{
    // How many pages the stack has (at least 1)
    int getPageCount(ItemStack stack);

    // The selected page, 0-based
    int getPage(ItemStack stack);

    // Selects a page. Called on both sides; the server's copy is the one that counts.
    void setPage(ItemStack stack, int page);

    // The page forward (+1) or back (-1) from the selected one, wrapping round
    default int nextPage(ItemStack stack, int direction)
    {
        int count = getPageCount(stack);
        // Math.floorMod keeps the result positive: floorMod(-1, 8) is 7, where -1 % 8 would be -1
        return count <= 0 ? 0 : Math.floorMod(getPage(stack) + direction, count);
    }

    // What to say over the hotbar when the page of the item in your hand changes (client/HeldPageMessages), e.g. a
    // Tag Getter's "Selected tag: #c:ingots". null: MowLib's own wording ("Page 3/8", or what's on the page for paged
    // storage items).
    default @Nullable Component describePage(ItemStack stack, int page)
    {
        return null;
    }
}
