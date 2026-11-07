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

    // The most of one resource a single slot may hold. normalLimit is what the storage allows on its own (items: the
    // item's max stack size, at most 99). Return more for bigger slots: ItemTransportType.Storage then holds up to
    // that many of one item per slot and saves the amounts itself (an ItemStack can only be saved up to 99).
    // Read every time, like getCapacity. Only item storages ask this so far.
    default <R extends Resource> int getSlotLimit(TransportType<R> type, R resource, int normalLimit)
    {
        return normalLimit;
    }

    // How many separate tanks a fluid storage has (FluidTransportType.Storage): each holds one fluid, up to
    // getCapacity(FLUIDS) mB. Read when the storage is made and by Storage#resize. At least 1.
    default int getTankCount(TransportType<?> type)
    {
        return 1;
    }

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
