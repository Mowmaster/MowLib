package com.mowmaster.mowlib.compacting;

import com.mowmaster.mowlib.registry.MowLibDataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ItemAccessResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

// An ICompactingItem as an item handler: one index per tier (nuggets, ingots, blocks), each showing how many of it the
// pool makes; putting a tier in or taking one out changes the pool by its value. An empty one has one index that takes
// the first item with a chain it allows (CompactingChains, ICompactingItem#allowsChain) and becomes that chain.
// `access` is where the item is (a shelf slot, a container...): the handler changes it only through that, so a
// cancelled transaction (a hopper only simulating) leaves it as it was. NeoForge's ItemAccessResourceHandler does the
// inserting/extracting; update() turns the new amount of one tier into the new pool. (Moved from Bibliomania.)
public class CompactingItemHandler extends ItemAccessResourceHandler<ItemResource>
{
    private final Item storageItem;

    public CompactingItemHandler(ItemAccess access)
    {
        super(access, Math.max(1, contents(access.getResource()).size()));
        this.storageItem = access.getResource().getItem();
    }

    private static CompactingContents contents(ItemResource storage)
    {
        // ItemResource#get: a component of the stack it stands for
        CompactingContents contents = storage.get(MowLibDataComponents.COMPACTING_CONTENTS.get());
        return contents == null ? CompactingContents.EMPTY : contents;
    }

    // Its contents, or EMPTY if another item has been swapped into the slot
    private CompactingContents current(ItemResource storage)
    {
        return storage.is(storageItem) ? contents(storage) : CompactingContents.EMPTY;
    }

    private int capacity(ItemResource storage)
    {
        ItemStack stack = storage.toStack(1);
        return stack.getItem() instanceof ICompactingItem item ? item.compactingCapacity(stack) : CompactingContents.DEFAULT_CAPACITY;
    }

    private boolean allows(ItemResource storage, List<CompactingContents.Tier> chain)
    {
        ItemStack stack = storage.toStack(1);
        return !(stack.getItem() instanceof ICompactingItem item) || item.allowsChain(stack, chain);
    }

    @Override
    protected ItemResource getResourceFrom(ItemResource storage, int index)
    {
        return current(storage).resource(index);
    }

    @Override
    protected int getAmountFrom(ItemResource storage, int index)
    {
        return current(storage).amount(index);
    }

    // Each index only takes its own tier (an index holding 0 would otherwise take anything); an empty one takes
    // anything with a chain it allows
    @Override
    public boolean isValid(int index, ItemResource resource)
    {
        ItemResource storage = itemAccess.getResource();
        if (!storage.is(storageItem)) return false;
        CompactingContents contents = current(storage);
        if (contents.isSet()) return resource.equals(contents.resource(index));
        if (index != 0 || !resource.getComponentsPatch().isEmpty()) return false;
        List<CompactingContents.Tier> chain = CompactingChains.find(resource.getItem());
        return chain != null && allows(storage, chain);
    }

    // What the index can hold: what it has plus the room left (an empty one: a full one of that item)
    @Override
    protected int getCapacity(int index, ItemResource resource)
    {
        ItemResource storage = itemAccess.getResource();
        CompactingContents contents = current(storage);
        if (contents.isSet()) return contents.amount(index) + contents.room(index);
        List<CompactingContents.Tier> chain = CompactingChains.find(resource.getItem());
        if (chain == null) return 0;
        CompactingContents fresh = CompactingContents.of(chain, 0, capacity(storage));
        return fresh.room(fresh.tierOf(resource));
    }

    // The item with tier `index` now at `amount`: the pool changes by the difference times the tier's value
    @Override
    protected ItemResource update(ItemResource storage, int index, ItemResource resource, int amount)
    {
        CompactingContents contents = current(storage);
        int tier = index;
        if (!contents.isSet())
        {
            List<CompactingContents.Tier> chain = CompactingChains.find(resource.getItem());
            if (chain == null) return ItemResource.EMPTY;
            contents = CompactingContents.of(chain, 0, capacity(storage));
            tier = contents.tierOf(resource);
            if (tier < 0) return ItemResource.EMPTY;
        }
        CompactingContents changed = contents.plus(tier, (long) amount - contents.amount(tier));
        return changed.isSet() ? storage.with(MowLibDataComponents.COMPACTING_CONTENTS.get(), changed)
                : storage.without(MowLibDataComponents.COMPACTING_CONTENTS.get());
    }
}
