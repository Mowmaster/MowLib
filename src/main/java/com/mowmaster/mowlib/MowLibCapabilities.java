package com.mowmaster.mowlib;

import com.mowmaster.mowlib.registry.MowLibBlockEntities;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

// Hoppers and other mods' pipes reach bulk storage through its item handler
@EventBusSubscriber(modid = MowLibReferences.MODID)
public class MowLibCapabilities
{
    @SubscribeEvent
    public static void registerCapabilities(RegisterCapabilitiesEvent event)
    {
        event.registerBlockEntity(Capabilities.Item.BLOCK, MowLibBlockEntities.BULK_STORAGE.get(),
                (storage, side) -> storage.getItems());
    }
}
