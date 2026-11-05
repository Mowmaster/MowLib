package com.mowmaster.mowlib.datagen;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.workcard.WorkCardType;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

// Every English string MowLib uses. This is the only source of en_us.json: there must be no hand-written
// src/main/resources/assets/mowlib/lang/en_us.json as well, or the build finds two files at the same path.
// Strings that are followed by a value in code (a mode name, a list type...) end in ": ".
public class MowLibLanguageProvider extends LanguageProvider
{
    private static final String MODID = MowLibReferences.MODID;

    public MowLibLanguageProvider(PackOutput output)
    {
        super(output, MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        add("itemGroup." + MODID, "MowLib");

        // ---- Colour picker and custom dye (items/CBColorPickerItem) ----
        addItem(MowLibItems.COLOR_PICKER, "Color Picker");
        addItem(MowLibItems.CUSTOM_DYE, "Custom Dye");
        add("message." + MODID + ".color_picker.picked", "Picked color %s");
        add("message." + MODID + ".color_picker.none", "This block has no color to pick");
        add("message." + MODID + ".color_picker.cleared", "Color cleared");
        // A Custom Dye crafted from a picker that knows its block: "Diamond Block Dye" (recipes/CBPickedDyeRecipe)
        add("item." + MODID + ".custom_dye.from_block", "%s Dye");

        // ---- Color Palette (palette/, client/ClientColorPaletteTooltip) ----
        addItem(MowLibItems.COLOR_PALETTE, "Color Palette");
        add("tooltip." + MODID + ".palette.selected", "Selected: %s");
        add("tooltip." + MODID + ".palette.empty", "No dyes");

        // ---- Page keys (client/MowLibKeyMappings) and the paged storage tooltip (client/ClientPagedContentsTooltip) ----
        add("key.category." + MODID + ".mowlib", "MowLib");
        add("key." + MODID + ".page_next", "Next Page");
        add("key." + MODID + ".page_previous", "Previous Page");
        add("tooltip." + MODID + ".paged.fullness", "Holds %1$s / %2$s");
        // How pages are worded everywhere: the tooltip, the message over the hotbar, the reading HUD (paging/PageText)
        add("page." + MODID + ".page", "Page %1$s/%2$s");
        add("page." + MODID + ".contents", "Page %1$s/%2$s: %3$s");
        add("page." + MODID + ".stack", "%1$s %2$s");
        add("page." + MODID + ".empty", "Empty");

        // ---- Transport types (TransportType#getName: transport_type.<namespace>.<path>) ----
        add("transport_type." + MODID + ".items", "Items");
        add("transport_type." + MODID + ".fluids", "Fluids");
        add("transport_type." + MODID + ".energy", "Energy");
        add("transport_type." + MODID + ".experience", "Experience");

        // ---- Tools (MowLibToolItem shows item.mowlib.<id>.tooltip) ----
        addItem(MowLibItems.TOOL_FILTER, "Filter Tool");
        add(MowLibItems.TOOL_FILTER.get().getDescriptionId() + ".tooltip", "Offhand + left-click a machine to take its filter out");
        addItem(MowLibItems.TOOL_WORK, "Work Card Tool");
        add(MowLibItems.TOOL_WORK.get().getDescriptionId() + ".tooltip", "Offhand + left-click a machine to take its work card out");
        add(MODID + ".tool_filter_missing", "There is no filter to remove");

        // ---- Filters (items/filters) ----
        addItem(MowLibItems.FILTER_BASE, "Blank Filter");
        addItem(MowLibItems.FILTER_ITEM, "Item Filter");
        addItem(MowLibItems.FILTER_ITEMSTACK, "Exact Item Filter");
        addItem(MowLibItems.FILTER_MOD, "Mod Filter");
        addItem(MowLibItems.FILTER_RESTRICTED, "Restricted Filter");
        addItem(MowLibItems.FILTER_DURABILITY, "Durability Filter");
        addItem(MowLibItems.FILTER_ENCHANTED, "Enchanted Filter");
        addItem(MowLibItems.FILTER_ENCHANTED_COUNT, "Enchantment Count Filter");
        addItem(MowLibItems.FILTER_ENCHANTED_EXACT, "Exact Enchantment Filter");
        addItem(MowLibItems.FILTER_ENCHANTED_FUZZY, "Any Enchantment Filter");
        addItem(MowLibItems.FILTER_FOOD, "Food Filter");
        addItem(MowLibItems.FILTER_TAG, "Tag Filter");
        addItem(MowLibItems.FILTER_TAG_MACHINE, "Tag Filter (Machines)");
        add(MODID + ".filter.tooltip_howto", "Click items onto it to list them; scroll to select one, right-click to remove it");
        add(MODID + ".filter.selected", "Selected: %s");
        // The pick-up / place keys at a block, holding a filter
        add(MODID + ".filter.block_added", "Added to the filter: %s");
        add(MODID + ".filter.block_removed", "Removed from the filter: %s");
        add(MODID + ".filter.block_missing", "%s isn't on the filter");
        add(MODID + ".filter.block_refused", "%s can't go on this filter");
        add(MODID + ".filter.list_full", "The filter's list is full");

        // ---- Tag Getter (items/filters/TagGetterItem) ----
        addItem(MowLibItems.TAG_GETTER, "Tag Getter");
        add(MODID + ".taggetter.created", "Tags read: %s");
        add(MODID + ".taggetter.none", "That has no tags");
        add(MODID + ".taggetter.clear", "Tags cleared");
        add(MODID + ".taggetter.selected", "Selected tag: %s");
        add(MODID + ".taggetter.empty", "No tags");
        add(MODID + ".taggetter.tooltip_header", "Tags of %s:");
        add(MODID + ".taggetter.tooltip_howto", "Click an item onto it to read its tags, or press the pick-up key at a block");

        add(MODID + ".filter.tooltip_filterdirection", "Applies to: ");
        add(MODID + ".tooltip_mode", "Mode: ");
        add(MODID + ".filter_type", "Type: ");
        add(MODID + ".filter_type_whitelist", "Whitelist");
        // Energy, experience and other amount-only modes (TransportType#isAmountOnly)
        add(MODID + ".filter_type_allowed", "Allowed");
        add(MODID + ".filter_type_disabled", "Disabled");
        add(MODID + ".filter_type_blacklist", "Blacklist");
        add(MODID + ".filter_type_above", "Above");
        add(MODID + ".filter_type_below", "Below");
        add(MODID + ".filter_mode_changed", "Filter mode: ");
        add(MODID + ".filter_type_changed", "Filter type: ");
        add(MODID + ".filter_changed", "Filter list updated");
        add(MODID + ".filter.message_twohanded", "Hold only one filter, in your offhand");
        // FilterDirection#getName: mowlib.filter_direction.<name in lower case>
        add(directionKey(FilterDirection.INSERT), "Inserting");
        add(directionKey(FilterDirection.EXTRACT), "Extracting");
        add(directionKey(FilterDirection.NEUTRAL), "Machine use");

        // ---- Work cards (items/workcards, workcard/WorkCardType) ----
        addItem(MowLibItems.WORKCARD_AREA, "Area Work Card");
        addItem(MowLibItems.WORKCARD_LOCATIONS, "Locations Work Card");
        add(MODID + ".workcard_blockpos_first", "First corner set");
        add(MODID + ".workcard_blockpos_second", "Second corner set");
        add(MODID + ".workcard_blockpos_full", "Both corners are set; right-click the air to clear the card");
        add(MODID + ".workcard_blockpos_added", "Location added");
        add(MODID + ".workcard_blockpos_removed", "Location removed");
        add(MODID + ".workcard_blockpos_clear", "Work card cleared");
        add(MODID + ".workcard_tooltip_blockpos_title", "Selected:");
        add(workCardKey(WorkCardType.AREA), "Area");
        add(workCardKey(WorkCardType.LOCATIONS), "Locations");
        add(workCardKey(WorkCardType.MACHINES), "Machines");
        add(workCardKey(WorkCardType.AREA_OR_LOCATIONS), "Area or Locations");

        // Action keys (client/KeyActionHandler, keys/) and activation messages
        add("key.mowlib.activate", "Activate item");
        add("key.mowlib.place", "Place held block");
        add("key.mowlib.pickup", "Pick up looked-at block");
        add("message.mowlib.activated", "%s activated");

        // Reading HUD lines (reading/ReadingPapers, ReadingLabels)
        add("hud.mowlib.filter", "Filter: %s");
        add("hud.mowlib.grows_up", "Grows up in: %s");
        add("hud.mowlib.breed_cooldown", "Can breed again in: %s");
        add("hud.mowlib.in_love", "In love");
        add("hud.mowlib.can_breed", "Ready to breed");

        // The client config screen (MowLibClientConfig)
        add("mowlib.configuration.title", "MowLib Settings");
        add("mowlib.configuration.section.mowlib.client.toml", "Screen settings");
        add("mowlib.configuration.section.mowlib.client.toml.title", "MowLib Screen Settings");
        add("mowlib.configuration.reading_paper", "Reading HUD: paper");
        add("mowlib.configuration.reading_label", "Reading HUD: label");
        add("mowlib.configuration.enabled", "Enabled");
        add("mowlib.configuration.anchor", "Screen position");
        add("mowlib.configuration.x_offset", "Move right (pixels)");
        add("mowlib.configuration.y_offset", "Move down (pixels)");
        add("mowlib.configuration.max_width", "Widest text (pixels)");
        add("mowlib.configuration.held_items", "Show held items");
        add("mowlib.configuration.show_amounts", "Show stored energy and fluids");
    }

    // The same keys FilterDirection#getName and WorkCardType#getName build
    private static String directionKey(FilterDirection direction)
    {
        return MODID + ".filter_direction." + direction.name().toLowerCase();
    }

    private static String workCardKey(WorkCardType type)
    {
        return MODID + ".workcard_type." + type.name().toLowerCase();
    }
}
