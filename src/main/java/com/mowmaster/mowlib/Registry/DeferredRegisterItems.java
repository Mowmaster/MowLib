package com.mowmaster.mowlib.Registry;


import com.mowmaster.mowlib.Items.BaseDustStorageItem;
import com.mowmaster.mowlib.Items.BaseRepairNote;
import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.Items.EffectItemBase;
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
            () -> new ColorApplicator(new Item.Properties().stacksTo(1)));

    public static final RegistryObject<Item> SCROLL_T2_REPAIR = ITEMS.register("repair_note",
            () -> new BaseRepairNote(new Item.Properties().stacksTo(1)));


    public static final RegistryObject<Item> ICON_HAND = ITEMS.register("icon_hand",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ICON_WORKSTATIONBASE = ITEMS.register("icon_workstationbase",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ICON_MACHINEBASE = ITEMS.register("icon_machinebase",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ICON_DUST = ITEMS.register("icon_dust",
            () -> new BaseDustStorageItem(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> ICON_EFFECT = ITEMS.register("icon_effect",
            () -> new EffectItemBase(new Item.Properties().stacksTo(1)));



    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
