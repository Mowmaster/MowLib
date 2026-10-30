package com.mowmaster.mowlib;

import com.mojang.logging.LogUtils;
import com.mowmaster.mowlib.registry.MowLibBlockEntities;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import org.slf4j.Logger;

// Shared code for Mowmaster's mods: tintable blocks (IColorableBlock and friends), the colour picker and custom dye,
// and bulk item storage. Mods depend on it (usually bundled with Jar-in-Jar) and use its classes and registries.
@Mod(MowLibReferences.MODID)
public class MowLib
{
    public static final Logger LOGGER = LogUtils.getLogger();

    public MowLib(IEventBus modEventBus, ModContainer modContainer)
    {
        MowLibDataComponents.register(modEventBus);
        TransportTypes.register(modEventBus);
        MowLibItems.register(modEventBus);
        MowLibBlockEntities.register(modEventBus);
        MowLibRecipes.register(modEventBus);
        modEventBus.addListener(this::addCreative);
    }

    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(MowLibItems.COLOR_PICKER);
            event.accept(MowLibItems.CUSTOM_DYE);
        }
    }
}
