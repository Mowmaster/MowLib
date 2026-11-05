package com.mowmaster.mowlib.reading;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.ItemEnchantments;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

// Who can see the reading HUD (client/reading/ReadingHud): a player whose helmet grants reading. MowLib adds no
// helmet or enchantment of its own; mods add theirs to two tags:
// - #mowlib:grants_reading (items, data/mowlib/tags/item/grants_reading.json): helmets that always grant it
//   (Bibliomania's Reading Glasses)
// - #mowlib:grants_reading (enchantments, data/mowlib/tags/enchantment/grants_reading.json): an enchantment on the
//   helmet that grants it (Bibliomania's Reading Goggles)
// Other ways (a curio slot, a potion effect...) can be added in code with addCondition. Checked on both sides: the
// server only answers HUD questions from players who can read.
public final class ReadingSight
{
    private ReadingSight() {}

    public static final TagKey<Item> GRANTING_ITEMS = TagKey.create(Registries.ITEM, MowLibReferences.id("grants_reading"));
    public static final TagKey<Enchantment> GRANTING_ENCHANTMENTS = TagKey.create(Registries.ENCHANTMENT, MowLibReferences.id("grants_reading"));

    // CopyOnWriteArrayList: safe to add to at any time, and read from both the client and server threads
    private static final List<Predicate<Player>> CONDITIONS = new CopyOnWriteArrayList<>();

    // Another way for a player to read (call it once, during mod setup)
    public static void addCondition(Predicate<Player> condition)
    {
        CONDITIONS.add(condition);
    }

    public static boolean canRead(Player player)
    {
        if (grantsReading(player.getItemBySlot(EquipmentSlot.HEAD))) return true;
        for (Predicate<Player> condition : CONDITIONS) if (condition.test(player)) return true;
        return false;
    }

    // Whether a helmet grants reading: it's in the item tag, or carries an enchantment in the enchantment tag
    public static boolean grantsReading(ItemStack helmet)
    {
        if (helmet.isEmpty()) return false;
        if (helmet.is(GRANTING_ITEMS)) return true;
        ItemEnchantments enchantments = helmet.getOrDefault(DataComponents.ENCHANTMENTS, ItemEnchantments.EMPTY);
        // keySet: the enchantments on it, as registry entries; Holder#is(tag) checks the tag
        for (Holder<Enchantment> enchantment : enchantments.keySet())
        {
            if (enchantment.is(GRANTING_ENCHANTMENTS)) return true;
        }
        return false;
    }
}
