package com.mowmaster.mowlib.filter;

import com.mowmaster.mowlib.transport.TransportType;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.resource.Resource;

// Implemented by filter items. A block entity holds one filter (see IFilterHolder); its storages ask it about every
// resource going in or out (TransportStorageHost#filterAllows). Each method takes the transport type, so filters
// work with types added by other mods too. (Replaces 1.20's IFilterItem, which had one method per mode.)
// "<R extends Resource>" on a method makes it generic per call: R is ItemResource for items, FluidResource for fluids...
public interface ITransportFilter
{
    FilterDirection getDirection(ItemStack filter);

    // Whether the resource may pass
    <R extends Resource> boolean allows(ItemStack filter, TransportType<R> type, R resource);

    // The most of this resource the holder may store with this filter in it (return capacity for no limit)
    default <R extends Resource> int limit(ItemStack filter, TransportType<R> type, R resource, int capacity)
    {
        return capacity;
    }

    // For a whitelist/blacklist indicator on the holder (the current mode's setting)
    boolean isBlacklist(ItemStack filter);

    // ---- The rules for a filter in a holder, the same wherever it's held (IFilterHolder for blocks, HeldFilters for
    // items). Static: called as ITransportFilter.applies(...), on any stack. ----

    // Whether this stack is a filter that applies to things moving this way: a filter only acts in the direction it's
    // set to (INSERT: what goes in; EXTRACT: what's taken out; NEUTRAL: what a machine works on)
    static boolean applies(ItemStack filter, FilterDirection direction)
    {
        return filter.getItem() instanceof ITransportFilter transportFilter && transportFilter.getDirection(filter) == direction;
    }

    // Whether a resource may move this way past this filter. No filter, or one set to another direction: allowed.
    static <R extends Resource> boolean allowsMoving(ItemStack filter, TransportType<R> type, R resource, FilterDirection direction)
    {
        if (!applies(filter, direction)) return true;
        return ((ITransportFilter) filter.getItem()).allows(filter, type, resource);
    }

    // The most of a resource the holder may store with this filter: only INSERT filters limit it
    static <R extends Resource> int limitStored(ItemStack filter, TransportType<R> type, R resource, int capacity)
    {
        if (!applies(filter, FilterDirection.INSERT)) return capacity;
        return ((ITransportFilter) filter.getItem()).limit(filter, type, resource, capacity);
    }
}
