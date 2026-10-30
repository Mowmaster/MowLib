package com.mowmaster.mowlib.datamaps;

import com.mojang.serialization.Codec;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.codecs.RecordCodecBuilder;

// One entry in the block_colors data map: { "color": "#RRGGBB" }
public record CBBlockColor(int rgb)
{
    // Reads and writes a colour as a "#RRGGBB" string
    private static final Codec<Integer> HEX_COLOR = Codec.STRING.comapFlatMap(
            text -> {
                String digits = text.startsWith("#") ? text.substring(1) : text;
                if (digits.length() != 6) return DataResult.error(() -> "Colour must be 6 hex digits: " + text);
                try
                {
                    return DataResult.success(Integer.parseInt(digits, 16));
                }
                catch (NumberFormatException e)
                {
                    return DataResult.error(() -> "Not a hex colour: " + text);
                }
            },
            rgb -> String.format("#%06X", rgb));

    public static final Codec<CBBlockColor> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            HEX_COLOR.fieldOf("color").forGetter(CBBlockColor::rgb)
    ).apply(instance, CBBlockColor::new));
}