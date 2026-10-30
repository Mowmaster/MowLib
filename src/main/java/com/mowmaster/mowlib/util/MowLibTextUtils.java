package com.mowmaster.mowlib.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.alchemy.PotionContents;
import org.jetbrains.annotations.Nullable;

// Small text helpers for tooltips, labels and chat
public final class MowLibTextUtils
{
    private MowLibTextUtils()
    {
    }

    // A potion's effects as its tooltip names them: "Harming II", "Speed, Strength"... (null if it has none)
    public static @Nullable Component potionEffects(ItemStack stack)
    {
        PotionContents contents = stack.get(DataComponents.POTION_CONTENTS);
        if (contents == null) return null;
        MutableComponent text = null;
        for (MobEffectInstance effect : contents.getAllEffects())
        {
            Component name = effect.getEffect().value().getDisplayName();
            // Vanilla's own keys: "potion.withAmplifier" is "%s %s", "potion.potency.1" is "II"...
            MutableComponent line = effect.getAmplifier() > 0
                    ? Component.translatable("potion.withAmplifier", name, Component.translatable("potion.potency." + effect.getAmplifier()))
                    : name.copy();
            if (text == null) text = line;
            else text.append(", ").append(line);
        }
        return text;
    }
}
