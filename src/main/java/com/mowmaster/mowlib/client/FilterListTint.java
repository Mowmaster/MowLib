package com.mowmaster.mowlib.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.items.filters.BaseFilterItem;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// An item model tint for filters: one colour while the filter's current mode is a whitelist (or "allowed"), another
// while it's a blacklist (or "disabled"). The filter model's frame faces use tintindex 1, so a filter's item
// definition lists it second:
//   "tints": [{"type": "minecraft:constant", "value": -1}, {"type": "mowlib:filter_list"}]
// In 1.20 a colour handler in code did this; since 1.21.4 item tints come only from the item definition's "tints".
// Colours are 0xAARRGGBB: -1 (opaque white) leaves the texture as it is; the default blacklist colour is black.
// Registered by MowLibClient.
public record FilterListTint(int whitelist, int blacklist) implements ItemTintSource
{
    public static final MapCodec<FilterListTint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.optionalFieldOf("whitelist", -1).forGetter(FilterListTint::whitelist),
                    Codec.INT.optionalFieldOf("blacklist", 0xFF000000).forGetter(FilterListTint::blacklist))
            .apply(instance, FilterListTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity)
    {
        return BaseFilterItem.isBlacklisted(stack) ? blacklist : whitelist;
    }

    @Override
    public MapCodec<FilterListTint> type()
    {
        return MAP_CODEC;
    }
}
