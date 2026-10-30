package com.mowmaster.mowlib.transport;

import com.mowmaster.mowlib.MowLibIds;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NewRegistryEvent;
import net.neoforged.neoforge.registries.RegistryBuilder;

// The "mowlib:transport_type" registry, so any mod can add transport types the same way it adds items, and the
// four built-in ones. Registry order is the order machines try types and filters cycle through modes.
public class TransportTypes
{
    public static final ResourceKey<Registry<TransportType<?>>> KEY = ResourceKey.createRegistryKey(MowLibIds.id("transport_type"));

    // sync(true): the server tells clients the ids, so both sides agree on every type
    public static final Registry<TransportType<?>> REGISTRY = new RegistryBuilder<>(KEY).sync(true).create();

    public static final DeferredRegister<TransportType<?>> TYPES = DeferredRegister.create(KEY, MowLibIds.MODID);

    public static final DeferredHolder<TransportType<?>, ItemTransportType> ITEMS = TYPES.register("items", ItemTransportType::new);
    public static final DeferredHolder<TransportType<?>, FluidTransportType> FLUIDS = TYPES.register("fluids", FluidTransportType::new);
    public static final DeferredHolder<TransportType<?>, EnergyTransportType> ENERGY = TYPES.register("energy", EnergyTransportType::new);
    public static final DeferredHolder<TransportType<?>, ExperienceTransportType> EXPERIENCE = TYPES.register("experience", ExperienceTransportType::new);

    // Call from MowLib's constructor with the mod event bus
    public static void register(IEventBus eventBus)
    {
        // The registry itself has to exist before anything (ours or another mod's) registers into it
        eventBus.addListener(NewRegistryEvent.class, event -> event.register(REGISTRY));
        TYPES.register(eventBus);
    }
}
