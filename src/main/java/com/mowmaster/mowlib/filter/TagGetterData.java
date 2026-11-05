package com.mowmaster.mowlib.filter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.Identifier;

import java.util.List;
import java.util.Optional;

// What a Tag Getter holds (the "mowlib:tag_getter" data component, replacing 1.20's stack NBT): the item its tags were
// read from, those tags' ids (sorted A to Z), and which one is selected. The tag filters match against the selected
// tag. Tags are kept as plain ids so one list can hold both item tags and the fluid tags of a bucket's fluid; a filter
// looks the id up as an item tag or a fluid tag depending on its mode.
// A record, so immutable: the with... methods return a changed copy.
public record TagGetterData(Optional<Identifier> source, List<Identifier> tags, int selected)
{
    public static final TagGetterData EMPTY = new TagGetterData(Optional.empty(), List.of(), 0);

    public static final Codec<TagGetterData> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Identifier.CODEC.optionalFieldOf("source").forGetter(TagGetterData::source),
                    Identifier.CODEC.listOf().optionalFieldOf("tags", List.of()).forGetter(TagGetterData::tags),
                    Codec.INT.optionalFieldOf("selected", 0).forGetter(TagGetterData::selected))
            .apply(instance, TagGetterData::new));

    // Sent to clients through the codec (tooltips need it); no registries involved
    public static final StreamCodec<ByteBuf, TagGetterData> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    // Compact constructor: copies the list so it can't be changed afterwards
    public TagGetterData
    {
        tags = List.copyOf(tags);
    }

    public boolean isEmpty()
    {
        return tags.isEmpty();
    }

    // Always inside the list (0 when empty)
    public int selectedIndex()
    {
        return tags.isEmpty() ? 0 : Math.floorMod(selected, tags.size());
    }

    public Optional<Identifier> selectedTag()
    {
        return tags.isEmpty() ? Optional.empty() : Optional.of(tags.get(selectedIndex()));
    }

    public TagGetterData withSelected(int index)
    {
        return new TagGetterData(source, tags, tags.isEmpty() ? 0 : Math.floorMod(index, tags.size()));
    }
}
