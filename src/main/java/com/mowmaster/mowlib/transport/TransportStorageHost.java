package com.mowmaster.mowlib.transport;

import com.mowmaster.mowlib.filter.FilterDirection;
import net.neoforged.neoforge.transfer.resource.Resource;

// What a storage asks of the thing it belongs to (a block entity, usually). Implement it on your block entity and
// pass it to TransportType#createStorage.
public interface TransportStorageHost
{
    // Call after the contents changed (on the root transaction commit), so the holder saves and, for types
    // that sync to clients, sends the update
    void onStorageChanged(TransportType<?> type);

    // How big this type's storage is, in the type's own units: items = number of stacks, fluids = mB,
    // energy = FE, experience = points. Read every time, so config or upgrade changes apply straight away.
    // Must already work while the storages are being made (createStorage is called from your constructor).
    int getCapacity(TransportType<?> type);

    // false if the holder's filter blocks this resource in this direction (no filter: always true).
    // IFilterHolder#filterAllows does this for a block entity holding one filter item.
    default <R extends Resource> boolean filterAllows(TransportType<R> type, R resource, FilterDirection direction)
    {
        return true;
    }

    // The most of this resource the filter allows (capacity if the filter doesn't limit it)
    default <R extends Resource> int filterLimit(TransportType<R> type, R resource, int capacity)
    {
        return capacity;
    }
}
