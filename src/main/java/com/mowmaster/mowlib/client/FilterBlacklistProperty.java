package com.mowmaster.mowlib.client;

import com.mojang.serialization.MapCodec;
import com.mowmaster.mowlib.items.filters.BaseFilterItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// 1 while a filter's current mode is a blacklist ("disabled" for energy and experience), 0 otherwise. Filter item
// definitions use it to swap to the plain filter icon when an amount-only mode is disabled:
//   "type": "minecraft:range_dispatch", "property": "mowlib:filter_blacklist",
//   "entries": [{"threshold": 0.5, "model": <disabled model>}], "fallback": <normal model>
// (A range property with two values does the job of a true/false one.) Registered by MowLibClient.
public record FilterBlacklistProperty() implements RangeSelectItemModelProperty
{
    // MapCodec.unit: no settings, so reading it from JSON always gives this one instance
    public static final MapCodec<FilterBlacklistProperty> MAP_CODEC = MapCodec.unit(new FilterBlacklistProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        return BaseFilterItem.isBlacklisted(stack) ? 1f : 0f;
    }

    @Override
    public MapCodec<FilterBlacklistProperty> type()
    {
        return MAP_CODEC;
    }
}
