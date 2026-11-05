package com.mowmaster.mowlib.transport;

import net.minecraft.world.item.ItemStack;

// An item that holds one transport type (a tank of fluid, a battery of energy, a tome of experience...) in its
// mowlib:transport_contents component, like Bibliomania's storage books. Open its contents with
// ItemTransportStorage.open(stack); give it bucket/bundle-style inventory clicks and a fill bar with
// TransportItemInteractions.
public interface ITransportStorageItem
{
    // The type it holds
    TransportType<?> storageType(ItemStack stack);

    // How much it holds (in the type's units: mB, FE, experience points...)
    int storageCapacity(ItemStack stack);
}
