package com.mowmaster.mowlib.client;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.tint.IDyeSource;
import net.minecraft.client.color.item.ItemTintSource;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// An item model tint: the colour an IDyeSource item paints with right now (the Color Palette's selected dye), or
// `default` when it has none. Used in an item model definition's "tints" list:
//   {"type": "mowlib:selected_dye", "default": -1}
// Registered by MowLibClient. Tint colours are 0xAARRGGBB; -1 is opaque white, which leaves the texture as it is.
public record SelectedDyeTint(int defaultColor) implements ItemTintSource
{
    // A MapCodec reads the tint's settings from the JSON object it sits in
    public static final MapCodec<SelectedDyeTint> MAP_CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                    Codec.INT.optionalFieldOf("default", -1).forGetter(SelectedDyeTint::defaultColor))
            .apply(instance, SelectedDyeTint::new));

    @Override
    public int calculate(ItemStack stack, @Nullable ClientLevel level, @Nullable LivingEntity entity)
    {
        Integer color = stack.getItem() instanceof IDyeSource source ? source.getDyeColor(stack) : null;
        return color != null ? 0xFF000000 | color : defaultColor;
    }

    @Override
    public MapCodec<SelectedDyeTint> type()
    {
        return MAP_CODEC;
    }
}
