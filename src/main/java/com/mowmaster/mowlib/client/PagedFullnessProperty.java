package com.mowmaster.mowlib.client;

import com.mojang.serialization.MapCodec;
import com.mowmaster.mowlib.paging.IPagedStorageItem;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.item.properties.numeric.RangeSelectItemModelProperty;
import net.minecraft.world.entity.ItemOwner;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// How full an IPagedStorageItem is, 0 to 1. Item models use it to show the item filling up:
//   "type": "minecraft:range_dispatch", "property": "mowlib:paged_fullness", "entries": [{"threshold": 0.5, ...}]
// Registered by MowLibClient. (Moved from Bibliomania's BookFullnessProperty, "bibliomania:book_fullness".)
public record PagedFullnessProperty() implements RangeSelectItemModelProperty
{
    // MapCodec.unit: the property has no settings, so reading it from JSON always gives this one instance
    public static final MapCodec<PagedFullnessProperty> MAP_CODEC = MapCodec.unit(new PagedFullnessProperty());

    @Override
    public float get(ItemStack stack, @Nullable ClientLevel level, @Nullable ItemOwner owner, int seed)
    {
        return stack.getItem() instanceof IPagedStorageItem paged ? paged.fullness(stack) : 0;
    }

    @Override
    public MapCodec<PagedFullnessProperty> type()
    {
        return MAP_CODEC;
    }
}
