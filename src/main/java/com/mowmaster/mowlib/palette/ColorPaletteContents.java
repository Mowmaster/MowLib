package com.mowmaster.mowlib.palette;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.tint.IDyeSource;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

// What a Color Palette holds (the "mowlib:color_palette" data component): one of each dye, always sorted by colour,
// and the index of the selected one (the dye the palette paints with). Like every data component it's immutable:
// the "with..." methods return a new ColorPaletteContents, which the item then sets on the stack.
// ItemStackTemplate is the immutable form of an ItemStack (item, count, data) with a proper equals, which a component
// needs; template.create() turns it back into a stack.
public record ColorPaletteContents(List<ItemStackTemplate> dyes, int selected)
{
    // The most different dyes one palette holds (16 vanilla dyes plus room for custom ones)
    public static final int MAX_DYES = 64;

    public static final ColorPaletteContents EMPTY = new ColorPaletteContents(List.of(), 0);

    public static final Codec<ColorPaletteContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    ItemStackTemplate.CODEC.listOf().optionalFieldOf("dyes", List.of()).forGetter(ColorPaletteContents::dyes),
                    Codec.INT.optionalFieldOf("selected", 0).forGetter(ColorPaletteContents::selected))
            .apply(instance, ColorPaletteContents::new));

    // Sent to clients through the codec (tooltips and the item's tint need it). "WithRegistries": items are registry entries.
    public static final StreamCodec<RegistryFriendlyByteBuf, ColorPaletteContents> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    // Compact constructor: runs before the fields are set. Copies the list so it can't be changed afterwards.
    public ColorPaletteContents
    {
        dyes = List.copyOf(dyes);
    }

    public int size()
    {
        return dyes.size();
    }

    public boolean isEmpty()
    {
        return dyes.isEmpty();
    }

    public boolean isFull()
    {
        return dyes.size() >= MAX_DYES;
    }

    // The selected index, always inside the list (0 when empty)
    public int selectedIndex()
    {
        return dyes.isEmpty() ? 0 : Math.floorMod(selected, dyes.size());
    }

    // The selected dye as a stack, or EMPTY
    public ItemStack selectedStack()
    {
        return dyes.isEmpty() ? ItemStack.EMPTY : dyes.get(selectedIndex()).create();
    }

    // The colour the palette paints with, or null when it's empty
    @Nullable
    public Integer selectedColor()
    {
        return dyes.isEmpty() ? null : IColorableBlock.dyeColorOf(selectedStack());
    }

    public ColorPaletteContents withSelected(int index)
    {
        return new ColorPaletteContents(dyes, dyes.isEmpty() ? 0 : Math.floorMod(index, dyes.size()));
    }

    // ---- What may go in ----

    // A real dye: anything IColorableBlock#dyeColorOf knows (vanilla and tagged dyes, Custom Dye), but not another
    // dye source such as a palette
    public static boolean isDye(ItemStack stack)
    {
        return !stack.isEmpty() && !(stack.getItem() instanceof IDyeSource) && IColorableBlock.dyeColorOf(stack) != null;
    }

    // Already holding this dye: same item and same colour. Custom Dyes of the same colour count as the same dye
    // even when they're named after different blocks.
    public boolean contains(ItemStack dye)
    {
        Integer color = IColorableBlock.dyeColorOf(dye);
        for (ItemStackTemplate held : dyes)
        {
            ItemStack stack = held.create();
            if (stack.getItem() == dye.getItem() && Objects.equals(IColorableBlock.dyeColorOf(stack), color)) return true;
        }
        return false;
    }

    // ---- Changing it ----

    // Adds one of the dye and re-sorts, keeping the same dye selected. Call only for a dye that isDye, isn't
    // contained yet, and when not full.
    public ColorPaletteContents withAdded(ItemStack dye)
    {
        List<ItemStackTemplate> sorted = new ArrayList<>(dyes);
        ItemStackTemplate added = ItemStackTemplate.fromNonEmptyStack(dye.copyWithCount(1));
        sorted.add(added);
        sorted.sort(BY_COLOR);
        // The first dye added becomes the selected one; otherwise the selection follows its dye to its new place
        ItemStackTemplate wasSelected = dyes.isEmpty() ? added : dyes.get(selectedIndex());
        return new ColorPaletteContents(sorted, sorted.indexOf(wasSelected));
    }

    // Takes the selected dye out. The selection stays at the same place (now the next dye along).
    public ColorPaletteContents withoutSelected()
    {
        if (dyes.isEmpty()) return this;
        List<ItemStackTemplate> left = new ArrayList<>(dyes);
        int index = selectedIndex();
        left.remove(index);
        return new ColorPaletteContents(left, left.isEmpty() ? 0 : Math.min(index, left.size() - 1));
    }

    // ---- Sorting by colour ----

    // Colours first, round the colour wheel (red, orange, yellow, green, blue, purple, pink), lighter before darker
    // for the same hue; then the greys from white to black. Comparator.comparingInt builds a comparator from a
    // number to compare by; thenComparing adds a tie-breaker.
    private static final Comparator<ItemStackTemplate> BY_COLOR =
            Comparator.comparingInt((ItemStackTemplate dye) -> isGrey(rgbOf(dye)) ? 1 : 0)
                    .thenComparingDouble(dye -> isGrey(rgbOf(dye)) ? 0 : hue(rgbOf(dye)))
                    .thenComparingDouble(dye -> -brightness(rgbOf(dye)));

    private static int rgbOf(ItemStackTemplate dye)
    {
        Integer color = IColorableBlock.dyeColorOf(dye.create());
        return color != null ? color : 0;
    }

    private static int red(int rgb) { return (rgb >> 16) & 0xFF; }
    private static int green(int rgb) { return (rgb >> 8) & 0xFF; }
    private static int blue(int rgb) { return rgb & 0xFF; }

    // 0-1: how bright the brightest channel is
    private static double brightness(int rgb)
    {
        return Math.max(red(rgb), Math.max(green(rgb), blue(rgb))) / 255.0;
    }

    // Close to grey (white, light grey, grey, black...): channels nearly equal, or too dark to tell
    private static boolean isGrey(int rgb)
    {
        int max = Math.max(red(rgb), Math.max(green(rgb), blue(rgb)));
        int min = Math.min(red(rgb), Math.min(green(rgb), blue(rgb)));
        return max < 25 || (max - min) < max * 0.15;
    }

    // 0-360 degrees round the colour wheel, red = 0 (the usual RGB -> HSB hue formula)
    private static double hue(int rgb)
    {
        double r = red(rgb) / 255.0, g = green(rgb) / 255.0, b = blue(rgb) / 255.0;
        double max = Math.max(r, Math.max(g, b));
        double min = Math.min(r, Math.min(g, b));
        double range = max - min;
        if (range == 0) return 0;
        double hue;
        if (max == r) hue = ((g - b) / range) % 6;
        else if (max == g) hue = (b - r) / range + 2;
        else hue = (r - g) / range + 4;
        hue *= 60;
        return hue < 0 ? hue + 360 : hue;
    }
}
