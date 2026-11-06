package com.mowmaster.mowlib.compacting;

import com.mowmaster.mowlib.filter.HeldFilters;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import net.minecraft.world.item.ItemStack;

import java.util.List;

// An item that is compacting storage (CompactingContents in its mowlib:compacting_contents component), like
// Bibliomania's Compacting Book. What MowLib gives such an item:
// - contents / setContents: read and write the component
// - CompactingItemHandler: it as an item handler (one index per tier), e.g. for a shelf or container holding it
// - Compacting.tooltip: "12 Block of Gold", "111 Gold Ingot"... lines
// - allowsChain: which chains an empty one may start (default: its HeldFilters filter, CompactingChains.filterAllows)
public interface ICompactingItem
{
    // How many of the top tier it holds when full
    default int compactingCapacity(ItemStack stack)
    {
        return CompactingContents.DEFAULT_CAPACITY;
    }

    // Whether an empty one may start this chain
    default boolean allowsChain(ItemStack self, List<CompactingContents.Tier> chain)
    {
        return CompactingChains.filterAllows(HeldFilters.get(self), chain);
    }

    static CompactingContents contents(ItemStack stack)
    {
        CompactingContents contents = stack.get(MowLibDataComponents.COMPACTING_CONTENTS.get());
        return contents == null ? CompactingContents.EMPTY : contents;
    }

    // Emptied contents take the component off (an empty storage has nothing to keep)
    static void setContents(ItemStack stack, CompactingContents contents)
    {
        if (contents.isSet()) stack.set(MowLibDataComponents.COMPACTING_CONTENTS.get(), contents);
        else stack.remove(MowLibDataComponents.COMPACTING_CONTENTS.get());
    }
}
