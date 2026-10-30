package com.mowmaster.mowlib.registry;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.items.CBColorPickerItem;
import com.mowmaster.mowlib.items.filters.DurabilityFilterItem;
import com.mowmaster.mowlib.items.filters.EnchantedCountFilterItem;
import com.mowmaster.mowlib.items.filters.EnchantedExactFilterItem;
import com.mowmaster.mowlib.items.filters.EnchantedFilterItem;
import com.mowmaster.mowlib.items.filters.EnchantedFuzzyFilterItem;
import com.mowmaster.mowlib.items.filters.FoodFilterItem;
import com.mowmaster.mowlib.items.filters.ItemFilterItem;
import com.mowmaster.mowlib.items.filters.ItemStackFilterItem;
import com.mowmaster.mowlib.items.filters.ModFilterItem;
import com.mowmaster.mowlib.items.filters.RestrictedFilterItem;
import com.mowmaster.mowlib.items.tools.FilterToolItem;
import com.mowmaster.mowlib.items.tools.MowLibToolItem;
import com.mowmaster.mowlib.items.workcards.WorkCardAreaItem;
import com.mowmaster.mowlib.items.workcards.WorkCardLocationsItem;
import com.mowmaster.mowlib.palette.ColorPaletteItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class MowLibItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MowLibReferences.MODID);

    // Right-click a block to store its colour; craft it alone for a Custom Dye of that colour
    public static final DeferredItem<Item> COLOR_PICKER = ITEMS.registerItem("color_picker",
            properties -> new CBColorPickerItem(properties.stacksTo(1)));
    // A dye of any colour, stored in its dyed_color component
    public static final DeferredItem<Item> CUSTOM_DYE = ITEMS.registerItem("custom_dye", Item::new);
    // Holds one of each dye; paints with the selected one
    public static final DeferredItem<ColorPaletteItem> COLOR_PALETTE = ITEMS.registerItem("color_palette", ColorPaletteItem::new);

    public static final DeferredItem<FilterToolItem> TOOL_FILTER = ITEMS.registerItem("tool_filter", FilterToolItem::new);
    public static final DeferredItem<MowLibToolItem> TOOL_WORK = ITEMS.registerItem("tool_work", MowLibToolItem::new);

    // ---- Filters ----
    // The blank filter is only a crafting ingredient for the others
    public static final DeferredItem<Item> FILTER_BASE = ITEMS.registerSimpleItem("filter_base");
    public static final DeferredItem<ItemFilterItem> FILTER_ITEM = ITEMS.registerItem("filter_item", ItemFilterItem::new);
    public static final DeferredItem<ItemStackFilterItem> FILTER_ITEMSTACK = ITEMS.registerItem("filter_itemstack", ItemStackFilterItem::new);
    public static final DeferredItem<ModFilterItem> FILTER_MOD = ITEMS.registerItem("filter_mod", ModFilterItem::new);
    public static final DeferredItem<RestrictedFilterItem> FILTER_RESTRICTED = ITEMS.registerItem("filter_restricted", RestrictedFilterItem::new);
    public static final DeferredItem<DurabilityFilterItem> FILTER_DURABILITY = ITEMS.registerItem("filter_durability", DurabilityFilterItem::new);
    public static final DeferredItem<EnchantedFilterItem> FILTER_ENCHANTED = ITEMS.registerItem("filter_enchanted", EnchantedFilterItem::new);
    public static final DeferredItem<EnchantedCountFilterItem> FILTER_ENCHANTED_COUNT = ITEMS.registerItem("filter_enchantedcount", EnchantedCountFilterItem::new);
    public static final DeferredItem<EnchantedExactFilterItem> FILTER_ENCHANTED_EXACT = ITEMS.registerItem("filter_enchantedexact", EnchantedExactFilterItem::new);
    public static final DeferredItem<EnchantedFuzzyFilterItem> FILTER_ENCHANTED_FUZZY = ITEMS.registerItem("filter_enchantedfuzzy", EnchantedFuzzyFilterItem::new);
    public static final DeferredItem<FoodFilterItem> FILTER_FOOD = ITEMS.registerItem("filter_food", FoodFilterItem::new);

    // For creative tabs, in display order
    public static final List<DeferredItem<? extends Item>> FILTERS = List.of(FILTER_BASE, FILTER_ITEM, FILTER_ITEMSTACK, FILTER_MOD,
            FILTER_RESTRICTED, FILTER_DURABILITY, FILTER_ENCHANTED, FILTER_ENCHANTED_COUNT, FILTER_ENCHANTED_EXACT,
            FILTER_ENCHANTED_FUZZY, FILTER_FOOD);

    // ---- Work cards ----
    public static final DeferredItem<WorkCardAreaItem> WORKCARD_AREA = ITEMS.registerItem("workcard_area", WorkCardAreaItem::new);
    public static final DeferredItem<WorkCardLocationsItem> WORKCARD_LOCATIONS = ITEMS.registerItem("workcard_locations", WorkCardLocationsItem::new);

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
