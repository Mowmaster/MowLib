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

    // ItemStackTemplate is the immutable form of an ItemStack (item, count, components) with a proper equals.
    // selected: the list item picked with scroll / the page keys (the one a right-click removes).
    public record Entry(boolean blacklist, List<ItemStackTemplate> list, int selected)
    {
        public static final Entry EMPTY = new Entry(false, List.of(), 0);

        public static final Codec<Entry> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.BOOL.optionalFieldOf("blacklist", false).forGetter(Entry::blacklist),
                ItemStackTemplate.CODEC.listOf().optionalFieldOf("list", List.of()).forGetter(Entry::list),
                Codec.INT.optionalFieldOf("selected", 0).forGetter(Entry::selected))
                .apply(instance, Entry::new));

        // The constructor from before `selected`, so code written against it keeps working (first item selected)
        public Entry(boolean blacklist, List<ItemStackTemplate> list)
        {
            this(blacklist, list, 0);
        }

        // Compact constructor: copies the list so it can't be changed afterwards
        public Entry
        {
            list = List.copyOf(list);
        }

        // Always inside the list (0 when empty)
        public int selectedIndex()
        {
            return list.isEmpty() ? 0 : Math.floorMod(selected, list.size());
        }

        public Entry withBlacklist(boolean blacklist)
        {
            return new Entry(blacklist, list, selected);
        }

        // A new list; the selection stays at the same place (moved back if the list got shorter)
        public Entry withList(List<ItemStackTemplate> list)
        {
            return new Entry(blacklist, list, Math.clamp(selected, 0, Math.max(0, list.size() - 1)));
        }

        // A new list with this item selected
        public Entry withList(List<ItemStackTemplate> list, int selected)
        {
            return new Entry(blacklist, list, Math.clamp(selected, 0, Math.max(0, list.size() - 1)));
        }

        public Entry withSelected(int index)
        {
            return new Entry(blacklist, list, list.isEmpty() ? 0 : Math.floorMod(index, list.size()));
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
