package com.mowmaster.mowlib.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStackTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

// A filter's settings, one Entry per transport type id: whitelist/blacklist and the list of items it was set
// up with. Stored on the filter item as the mowlib:filter_data component (replaces 1.20's per-mode NBT keys).
// Components must be immutable: "changing" one means making a new FilterData and setting it on the stack.
// Type ids are saved as the type's current registry name: loading turns an old (aliased) id, such as Pedestals'
// earlier pedestals:items, into the type it now points at (mowlib:items).
public record FilterData(Map<Identifier, Entry> entries)
{
    public static final FilterData EMPTY = new FilterData(Map.of());

    // ItemStackTemplate is the immutable form of an ItemStack (item, count, components) with a proper equals
    public record Entry(boolean blacklist, List<ItemStackTemplate> list)
    {
        public static final Entry EMPTY = new Entry(false, List.of());

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(Entry::blacklist),
                ItemStackTemplate.CODEC.listOf().optionalFieldOf("list", List.of()).forGetter(Entry::list))
                .apply(instance, Entry::new));

        public Entry withBlacklist(boolean blacklist)
        {
            return new Entry(blacklist, list);
        }

        public Entry withList(List<ItemStackTemplate> list)
        {
            return new Entry(blacklist, List.copyOf(list));
        }
    }

    public static final Codec<FilterData> CODEC = Codec.unboundedMap(Identifier.CODEC, Entry.CODEC)
            .xmap(FilterData::resolved, FilterData::entries);
    // How it's sent to clients (tooltips and the item model need it)
    public static final StreamCodec<RegistryFriendlyByteBuf, FilterData> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public FilterData
    {
        entries = Map.copyOf(entries); // compact constructor: runs before the fields are set, keeps the map immutable
    }

    // Each key as its type's current id (registry aliases resolved); unknown ids are kept as they are
    private static FilterData resolved(Map<Identifier, Entry> entries)
    {
        Map<Identifier, Entry> resolved = new HashMap<>();
        entries.forEach((id, entry) -> {
            TransportType<?> type = TransportTypes.REGISTRY.getValue(id);
            resolved.put(type != null ? type.getId() : id, entry);
        });
        return new FilterData(resolved);
    }

    public Entry get(Identifier type)
    {
        return entries.getOrDefault(type, Entry.EMPTY);
    }

    public FilterData with(Identifier type, Entry entry)
    {
        Map<Identifier, Entry> copy = new HashMap<>(entries);
        copy.put(type, entry);
        return new FilterData(copy);
    }
}
