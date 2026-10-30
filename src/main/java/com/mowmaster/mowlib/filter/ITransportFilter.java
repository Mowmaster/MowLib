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
}
