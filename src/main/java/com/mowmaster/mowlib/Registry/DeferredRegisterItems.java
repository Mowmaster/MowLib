package com.mowmaster.mowlib.Registry;


import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.Tabs.MowLibTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;


public class DeferredRegisterItems
{
    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, MODID);

    public static final RegistryObject<Item> COLOR_APPLICATOR = ITEMS.register("applicator",
            () -> new ColorApplicator(new Item.Properties().stacksTo(1).tab(MowLibTab.TAB_ITEMS)));

    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
