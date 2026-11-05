package com.mowmaster.mowlib.picker;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

// A "blank" item that turns into one of a set of items, picked from a radial menu (client/ItemPickerScreen): the
// Blank Filter into any filter, the Blank Work Card into any work card, or any group another mod registers with
// ItemPicker.register. Picking swaps one blank (held in a hand) for the item picked.
// id:            names the group (the menu's title is the lang key picker.<namespace>.<path>, e.g. picker.mowlib.filters)
// base:          the blank item (a Supplier, e.g. a DeferredItem, so it's only looked up once items exist)
// isChoice:      which items it can become, from every registered item (any mod's: e.g. every ITransportFilter)
// activationKey: whether MowLib's activation key opens the menu while holding the blank (other ways: ItemPicker.open)
// resetRecipe:   whether any choice crafted alone turns back into the blank (recipes/PickerResetRecipe)
// A record: an immutable holder whose constructor, getters, equals and toString Java writes for us.
public record PickerGroup(Identifier id, Supplier<? extends Item> base, Predicate<Item> isChoice, boolean activationKey, boolean resetRecipe)
{
    public boolean isBase(ItemStack stack)
    {
        return !stack.isEmpty() && stack.is(base.get());
    }

    // Every item it can become, in registry order (MowLib's, then other mods')
    public List<Item> choices()
    {
        return BuiltInRegistries.ITEM.stream().filter(isChoice).toList();
    }

    // "Pick a filter": lang key picker.<namespace>.<path>
    public Component title()
    {
        return Component.translatable("picker." + id.getNamespace() + "." + id.getPath());
    }
}
