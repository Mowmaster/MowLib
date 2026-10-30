package com.mowmaster.mowlib.paging;

import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.storage.CBBulkItemHandler;
import com.mowmaster.mowlib.storage.StorageInteractions;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

// An item that stores items on pages (mowlib:paged_contents), like a bundle with one item type per page.
// Implement it on the Item class (it works for BlockItems too) and give the four settings; everything else has a
// default. To get the bundle-style inventory clicks and the page tooltip, override three Item methods to call
// PagedStorageInteractions (see its comment). Page selection (scroll / page keys) comes from IPageable for free,
// and the "mowlib:paged_fullness" item model property reads fullness().
// Interface default methods (Java 8+) give implementing classes a ready-made body they can still override.
public interface IPagedStorageItem extends IPageable
{
    // ---- Settings ----

    // Items per page
    int pageCapacity(ItemStack stack);

    // Pages when the stack has no mowlib:paged_contents yet
    int defaultPages(ItemStack stack);

    // Whether this item may go in (e.g. refuse other storage items)
    boolean accepts(ItemStack self, ItemResource resource);

    // Whether players can put items in by hand in inventories (taking out always works)
    default boolean allowsInsert(ItemStack self)
    {
        return true;
    }

    // ---- Contents ----

    default PagedContents contents(ItemStack stack)
    {
        PagedContents contents = stack.get(MowLibDataComponents.PAGED_CONTENTS.get());
        return contents != null ? contents : PagedContents.empty(defaultPages(stack));
    }

    default void setContents(ItemStack stack, PagedContents contents)
    {
        stack.set(MowLibDataComponents.PAGED_CONTENTS.get(), contents);
    }

    default float fullness(ItemStack stack)
    {
        return contents(stack).fullness(pageCapacity(stack));
    }

    // ---- IPageable ----

    @Override
    default int getPageCount(ItemStack stack)
    {
        return contents(stack).pages();
    }

    @Override
    default int getPage(ItemStack stack)
    {
        return contents(stack).selectedPage();
    }

    @Override
    default void setPage(ItemStack stack, int page)
    {
        setContents(stack, contents(stack).withPage(page));
    }

    // ---- Putting items in and taking them out ----

    // Stores as much of the stack as fits, onto pages already holding the same item first, then empty ones.
    // Does not change the stack. With simulate, only works out how much would fit. Returns how many went in (or would).
    default int insert(ItemStack self, ItemStack stack, boolean simulate)
    {
        if (stack.isEmpty() || !allowsInsert(self)) return 0;
        ItemResource resource = ItemResource.of(stack);
        if (!accepts(self, resource)) return 0;

        PagedContents contents = contents(self);
        // A copy of the pages to insert into; StorageInteractions.insertStack fills pages holding something first,
        // then empty ones (NeoForge's insertStacking; a page holding another item takes nothing)
        CBBulkItemHandler items = contents.toHandler(pageCapacity(self), r -> accepts(self, r), () -> {});
        int inserted = StorageInteractions.insertStack(items, stack, simulate);
        if (!simulate && inserted > 0)
        {
            setContents(self, new PagedContents(contents.pages(), contents.page(), items.copyToList()));
        }
        return inserted;
    }

    // Takes up to one stack off the selected page (nothing if that page is empty)
    default ItemStack extractSelected(ItemStack self)
    {
        PagedContents contents = contents(self);
        CBBulkItemHandler items = contents.toHandler(pageCapacity(self), r -> accepts(self, r), () -> {});
        int page = contents.selectedPage();
        ItemResource resource = items.getResource(page);
        if (resource.isEmpty()) return ItemStack.EMPTY;

        try (Transaction transaction = Transaction.openRoot())
        {
            int extracted = items.extract(page, resource, resource.getMaxStackSize(), transaction);
            transaction.commit();
            setContents(self, new PagedContents(contents.pages(), page, items.copyToList()));
            return resource.toStack(extracted);
        }
    }
}
