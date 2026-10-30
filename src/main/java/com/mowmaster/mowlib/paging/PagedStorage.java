package com.mowmaster.mowlib.paging;

import com.mowmaster.mowlib.storage.CBBulkItemHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;

import java.util.function.Predicate;

// Paged item storage for a block entity: one CBBulkItemHandler slot per page, plus which page is open. Like CBTintData,
// the block entity owns one of these and forwards to it:
// - saveAdditional/loadAdditional -> save/load (keys "pages", "page", and "stacks" from CBBulkItemHandler)
// - applyImplicitComponents       -> load(PagedContents) with the item's mowlib:paged_contents
// - collectImplicitComponents     -> toContents()
// - IPageHolder methods           -> getPage/getPageCount/setPage
// Page changes are saved and sent to clients here (the owner's getUpdatePacket must include saveAdditional's data,
// e.g. via saveCustomOnly); content changes also call the owner's onChanged.
// (Moved from Bibliomania's BookBlockEntity.)
public class PagedStorage
{
    private static final String TAG_PAGES = "pages";
    private static final String TAG_PAGE = "page";

    private final BlockEntity owner;
    private final int pageCapacity;
    private final int defaultPages;
    private final Predicate<ItemResource> accepts;
    private final Runnable onChanged;
    // Not final: the page count can come from the item when the block is placed, so the handler is rebuilt then
    private CBBulkItemHandler items;
    private int page = 0;

    // pageCapacity = items per page; defaultPages = pages when nothing says otherwise; accepts = what may go in;
    // onChanged = called after every change to the contents (e.g. to update the block state or comparators)
    public PagedStorage(BlockEntity owner, int pageCapacity, int defaultPages, Predicate<ItemResource> accepts, Runnable onChanged)
    {
        this.owner = owner;
        this.pageCapacity = pageCapacity;
        this.defaultPages = defaultPages;
        this.accepts = accepts;
        this.onChanged = onChanged;
        this.items = newHandler(defaultPages);
    }

    private CBBulkItemHandler newHandler(int pages)
    {
        return new CBBulkItemHandler(pages, pageCapacity, accepts, this::contentsChanged);
    }

    // ---- Pages ----

    public CBBulkItemHandler getItems()
    {
        return items;
    }

    public int getPageCapacity()
    {
        return pageCapacity;
    }

    public int getPageCount()
    {
        return items.size();
    }

    public int getPage()
    {
        return page;
    }

    public void setPage(int newPage)
    {
        page = Math.floorMod(newPage, getPageCount());
        owner.setChanged();
        sync();
    }

    public void turnPage(int direction)
    {
        setPage(page + direction);
    }

    // What the open page holds, as a normal ItemStack (e.g. for a renderer); at most one stack's worth
    public ItemStack getOpenPageStack()
    {
        ItemResource resource = items.getResource(page);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        return resource.toStack((int) Math.min(items.getAmountAsLong(page), resource.getMaxStackSize()));
    }

    // Puts as much of the stack as fits into the open page (does not change the stack). Returns how many went in.
    public int insertIntoOpenPage(ItemStack stack)
    {
        if (stack.isEmpty()) return 0;
        // try-with-resources: the transaction is closed (and undone if not committed) at the end of the block
        try (Transaction transaction = Transaction.openRoot())
        {
            int inserted = items.insert(page, ItemResource.of(stack), stack.getCount(), transaction);
            transaction.commit();
            return inserted;
        }
    }

    // Takes up to maxAmount (and at most one normal stack) out of the open page
    public ItemStack extractFromOpenPage(int maxAmount)
    {
        ItemResource resource = items.getResource(page);
        if (resource.isEmpty()) return ItemStack.EMPTY;
        int amount = Math.min(maxAmount, resource.getMaxStackSize());
        try (Transaction transaction = Transaction.openRoot())
        {
            int extracted = items.extract(page, resource, amount, transaction);
            transaction.commit();
            return resource.toStack(extracted);
        }
    }

    private void contentsChanged()
    {
        owner.setChanged();
        onChanged.run();
        sync();
    }

    // Sends the owner's getUpdatePacket() to nearby clients
    private void sync()
    {
        Level level = owner.getLevel();
        if (level != null && !level.isClientSide())
        {
            level.sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // ---- Item <-> block ----

    public PagedContents toContents()
    {
        return new PagedContents(getPageCount(), page, items.copyToList());
    }

    // Replaces everything with what an item carried
    public void load(PagedContents contents)
    {
        items = contents.toHandler(pageCapacity, accepts, this::contentsChanged);
        page = contents.selectedPage();
        contentsChanged();
    }

    // ---- Saving ----

    public void save(ValueOutput output)
    {
        output.putInt(TAG_PAGES, getPageCount());
        output.putInt(TAG_PAGE, page);
        items.serialize(output);
    }

    public void load(ValueInput input)
    {
        items = newHandler(input.getIntOr(TAG_PAGES, defaultPages));
        // deserialize replaces the slot list with the saved one (and its size)
        items.deserialize(input);
        page = Math.floorMod(input.getIntOr(TAG_PAGE, 0), getPageCount());
    }
}
