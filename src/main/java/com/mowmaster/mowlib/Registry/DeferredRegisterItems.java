package com.mowmaster.mowlib.Registry;


import com.mowmaster.mowlib.Items.BaseDustStorageItem;
import com.mowmaster.mowlib.Items.BaseRepairNote;
import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.Items.EffectItemBase;
import com.mowmaster.mowlib.Items.Filters.*;
import com.mowmaster.mowlib.Items.Tools.DevTool;
import com.mowmaster.mowlib.Items.Tools.FilterTool;
import com.mowmaster.mowlib.Items.Tools.TagTool;
import com.mowmaster.mowlib.Items.Tools.WorkCardTool;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardArea;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardLocations;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBE;
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

    public static final RegistryObject<Item> FILTER_BASE = ITEMS.register("filter_base",
            () -> new FilterBaseItem(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ITEM = ITEMS.register("filter_item",
            () -> new FilterItem(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ITEMSTACK = ITEMS.register("filter_itemstack",
            () -> new FilterItemStack(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_DURABILITY = ITEMS.register("filter_durability",
            () -> new FilterDurability(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ENCHANTED = ITEMS.register("filter_enchanted",
            () -> new FilterEnchanted(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ENCHANTED_COUNT = ITEMS.register("filter_enchantedcount",
            () -> new FilterEnchantCount(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ENCHANTED_EXACT = ITEMS.register("filter_enchantedexact",
            () -> new FilterEnchantedExact(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ENCHANTED_FUZZY = ITEMS.register("filter_enchantedfuzzy",
            () -> new FilterEnchantedFuzzy(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_FOOD = ITEMS.register("filter_food",
            () -> new FilterFood(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_MOD = ITEMS.register("filter_mod",
            () -> new FilterMod(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_RESTRICTED = ITEMS.register("filter_restricted",
            () -> new FilterRestricted(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_TAG = ITEMS.register("filter_tag",
            () -> new FilterTag(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_TAG_MACHINE = ITEMS.register("filter_tag_machine",
            () -> new FilterTagMachine(new Item.Properties()));
    public static final RegistryObject<Item> TAG_GETTER = ITEMS.register("tag_getter",
            () -> new TagGetterItem(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ITEM_MACHINE = ITEMS.register("filter_item_machine",
            () -> new FilterItemMachine(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_ITEMSTACK_MACHINE = ITEMS.register("filter_itemstack_machine",
            () -> new FilterItemStackMachine(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_BLOCKS_ON_CLICK_EXACT = ITEMS.register("filter_blocksonclickexact",
            () -> new FilterBlocksByClickExact(new Item.Properties()));
    public static final RegistryObject<Item> FILTER_BLOCKS_ON_CLICK_FUZZY = ITEMS.register("filter_blocksonclickfuzzy",
            () -> new FilterBlocksByClickFuzzy(new Item.Properties()));

    public static final RegistryObject<Item> WORKCARD_LOCATIONS = ITEMS.register("workcard_locations",
            () -> new WorkCardLocations(new Item.Properties()));
    public static final RegistryObject<Item> WORKCARD_AREA = ITEMS.register("workcard_area",
            () -> new WorkCardArea(new Item.Properties()));
    public static final RegistryObject<Item> WORKCARD_BLOCKENTITY_LOCATIONS = ITEMS.register("workcard_belocations",
            () -> new WorkCardBE(new Item.Properties()));


    public static final RegistryObject<Item> TOOL_FILTERTOOL = ITEMS.register("tool_filter",
            () -> new FilterTool(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_WORKTOOL = ITEMS.register("tool_work",
            () -> new WorkCardTool(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_TAGTOOL = ITEMS.register("tool_tag",
            () -> new TagTool(new Item.Properties().stacksTo(1)));
    public static final RegistryObject<Item> TOOL_DEVTOOL = ITEMS.register("tool_dev",
            () -> new DevTool(new Item.Properties().stacksTo(1)));




    public static void register(IEventBus eventBus) {
        ITEMS.register(eventBus);
    }
}
