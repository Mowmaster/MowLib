package com.mowmaster.mowlib.compacting;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

// Compacting storage: one item family's chain of tiers (gold nugget -> ingot -> block) and ONE pool counted in the
// lowest tier. Every tier is a view of that pool: putting in a block adds 81 nuggets' worth, taking out a nugget takes
// 1, and each tier shows how many of it the pool makes (total / its value). Nothing is ever crafted, so nothing is lost.
// - capacity: how many of the TOP tier fit (1024 gold blocks = 82,944 nuggets; 1024 amethyst blocks = 4,096 shards)
// - an empty storage has no chain; the first item in sets it (CompactingChains.find), and emptying it clears it again
// - page: the tier that's open (for paged blocks and items: arrows, page keys)
// Kept on items as mowlib:compacting_contents (a record: immutable, with equals/hashCode, as components must be); block
// entities save it with CODEC. (Moved from Bibliomania's Compacting Book.)
public record CompactingContents(List<Tier> tiers, long total, int page, int capacity)
{
    // The usual capacity (and what contents saved before capacity existed load with)
    public static final int DEFAULT_CAPACITY = 1024;

    // One tier: its item and how many of the lowest tier it's worth (the lowest is 1)
    public record Tier(Item item, int value)
    {
        public static final Codec<Tier> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                BuiltInRegistries.ITEM.byNameCodec().fieldOf("item").forGetter(Tier::item),
                Codec.INT.fieldOf("value").forGetter(Tier::value)
        ).apply(instance, Tier::new));
    }

    public static final CompactingContents EMPTY = new CompactingContents(List.of(), 0, 0, DEFAULT_CAPACITY);

    public static final Codec<CompactingContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Tier.CODEC.listOf().fieldOf("tiers").forGetter(CompactingContents::tiers),
            Codec.LONG.fieldOf("total").forGetter(CompactingContents::total),
            Codec.INT.optionalFieldOf("page", 0).forGetter(CompactingContents::page),
            Codec.INT.optionalFieldOf("capacity", DEFAULT_CAPACITY).forGetter(CompactingContents::capacity)
    ).apply(instance, CompactingContents::new));
    // fromCodecWithRegistries: sent to clients the same way it's saved
    public static final StreamCodec<RegistryFriendlyByteBuf, CompactingContents> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public CompactingContents
    {
        tiers = List.copyOf(tiers); // compact constructor: keeps the list immutable
    }

    // A fresh storage set to a chain (nothing in it yet), open at `page`, holding `capacity` of the top tier
    public static CompactingContents of(List<Tier> tiers, int page, int capacity)
    {
        return new CompactingContents(tiers, 0, Math.max(0, Math.min(page, tiers.size() - 1)), capacity);
    }

    public boolean isSet()
    {
        return !tiers.isEmpty();
    }

    public int size()
    {
        return tiers.size();
    }

    // The most the pool can hold, in the lowest tier
    public long maxTotal()
    {
        return isSet() ? (long) tiers.getLast().value() * capacity : 0;
    }

    public float fullness()
    {
        return isSet() && maxTotal() > 0 ? (float) ((double) total / maxTotal()) : 0f;
    }

    public ItemResource resource(int tier)
    {
        return tier >= 0 && tier < tiers.size() ? ItemResource.of(tiers.get(tier).item()) : ItemResource.EMPTY;
    }

    public ItemStack stack(int tier, int count)
    {
        return tier >= 0 && tier < tiers.size() ? new ItemStack(tiers.get(tier).item(), count) : ItemStack.EMPTY;
    }

    // How many of a tier the pool makes
    public int amount(int tier)
    {
        if (tier < 0 || tier >= tiers.size()) return 0;
        return (int) Math.min(Integer.MAX_VALUE, total / tiers.get(tier).value());
    }

    // How many more of a tier fit
    public int room(int tier)
    {
        if (tier < 0 || tier >= tiers.size()) return 0;
        return (int) Math.min(Integer.MAX_VALUE, (maxTotal() - total) / tiers.get(tier).value());
    }

    // Which tier an item is (exactly: no extra components), or -1
    public int tierOf(ItemResource resource)
    {
        for (int tier = 0; tier < tiers.size(); tier++)
        {
            if (resource.equals(resource(tier))) return tier;
        }
        return -1;
    }

    // count of a tier added (negative: taken). Emptied: no chain any more, so it can take another family.
    public CompactingContents plus(int tier, long count)
    {
        long changed = Math.max(0, Math.min(maxTotal(), total + count * tiers.get(tier).value()));
        return changed == 0 ? EMPTY.withCapacity(capacity) : new CompactingContents(tiers, changed, page, capacity);
    }

    public CompactingContents withPage(int newPage)
    {
        return isSet() ? new CompactingContents(tiers, total, Math.floorMod(newPage, tiers.size()), capacity) : this;
    }

    // The same with another capacity (what's in it over the new room is kept until taken out)
    public CompactingContents withCapacity(int newCapacity)
    {
        return newCapacity == capacity ? this : new CompactingContents(tiers, total, page, newCapacity);
    }
}
