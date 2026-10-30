package com.mowmaster.mowlib.filter;

import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

// Something that holds one filter item (a block entity, usually). Implement the three item methods; the rest
// (what the filter allows, and its limit) works from them, so a TransportStorageHost can simply forward:
//   public <R extends Resource> boolean filterAllows(TransportType<R> type, R resource, FilterDirection direction)
//   { return IFilterHolder.super.filterAllows(type, resource, direction); }
// The filter tool takes it out (StorageInteractions#removeFilter).
public interface IFilterHolder
{
    ItemStack getFilter();

    // Takes one filter from the stack; false if there's already one or it isn't a filter
    boolean insertFilter(ItemStack stack);

    // Takes the filter out (EMPTY if there's none)
    ItemStack removeFilter();

    default boolean hasFilter()
    {
        return !getFilter().isEmpty();
    }

    // Only a filter set to this direction applies; no filter allows everything
    default <R extends Resource> boolean filterAllows(TransportType<R> type, R resource, FilterDirection direction)
    {
        ItemStack filter = getFilter();
        if (!(filter.getItem() instanceof ITransportFilter transportFilter) || transportFilter.getDirection(filter) != direction) return true;
        return transportFilter.allows(filter, type, resource);
    }

    // Limits only come from INSERT filters
    default <R extends Resource> int filterLimit(TransportType<R> type, R resource, int capacity)
    {
        ItemStack filter = getFilter();
        if (!(filter.getItem() instanceof ITransportFilter transportFilter) || transportFilter.getDirection(filter) != FilterDirection.INSERT) return capacity;
        return transportFilter.limit(filter, type, resource, capacity);
    }

    // For machines that read the filter themselves (breakers, placers...): only NEUTRAL filters apply
    default boolean machineFilterAllows(ItemStack stack)
    {
        return stack.isEmpty() || filterAllows(TransportTypes.ITEMS.get(), ItemResource.of(stack), FilterDirection.NEUTRAL);
    }

    // 0 = no filter, 1 = whitelist, 2 = blacklist (e.g. for a blockstate property)
    default int filterStatus()
    {
        ItemStack filter = getFilter();
        if (!(filter.getItem() instanceof ITransportFilter transportFilter)) return 0;
        return transportFilter.isBlacklist(filter) ? 2 : 1;
    }
}
