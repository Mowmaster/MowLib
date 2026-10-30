package com.mowmaster.mowlib;

import com.mowmaster.mowlib.creative.CreativeTabRegistry;
import com.mowmaster.mowlib.registry.MowLibBlockEntities;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

// Shared code for Mowmaster's mods (see CLAUDE.md "What's here"). Mods depend on it, bundle it with Jar-in-Jar, and
// use its classes and registries. MowLib registers mechanisms only, never blocks: the shared block entity types
// (MowLibBlockEntities) pick up every mod's matching blocks by themselves.
//
// Everything that needs registering is registered here (DeferredRegisters) or by an @EventBusSubscriber class:
// MowLibCapabilities, MowLibNetwork, CBDataMaps, MowLibClient (client only), KnappingHandler, BonusDropHandler.
@Mod(MowLibReferences.MODID)
public class MowLib
{
    public MowLib(IEventBus modEventBus, ModContainer modContainer)
    {
        // Data components before items: filter items put a default filter_mode on themselves when they're made
        MowLibDataComponents.register(modEventBus);
        TransportTypes.register(modEventBus);
        MowLibItems.register(modEventBus);
        MowLibBlockEntities.register(modEventBus);
        MowLibRecipes.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
    }
}
