package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MowLibComponentUtils
{
    public static MutableComponent getBlockPosFormatted(BlockPos pos){
        MutableComponent posOneX = Component.literal("" + pos.getX());
        MutableComponent posOneY = Component.literal("" + pos.getY());
        MutableComponent posOneZ = Component.literal("" + pos.getZ());
        MutableComponent getX = Component.translatable(MODID + ".text.separator.x");
        MutableComponent getY = Component.translatable(MODID + ".text.separator.y");
        MutableComponent getZ = Component.translatable(MODID + ".text.separator.z");
        posOneX.append(getX);
        posOneX.append(posOneY);
        posOneX.append(getY);
        posOneX.append(posOneZ);
        posOneX.append(getZ);

        return posOneX;
    }

    public static Component getEnchantRomanNumeral(int enchantPotency)
    {
        switch(enchantPotency)
        {
            case 0:return Component.translatable(MODID + ".text.numeral.1");
            case 1:return Component.translatable(MODID + ".text.numeral.2");
            case 2:return Component.translatable(MODID + ".text.numeral.3");
            case 3:return Component.translatable(MODID + ".text.numeral.4");
            case 4:return Component.translatable(MODID + ".text.numeral.5");
            case 5:return Component.translatable(MODID + ".text.numeral.6");
            case 6:return Component.translatable(MODID + ".text.numeral.7");
            case 7:return Component.translatable(MODID + ".text.numeral.8");
            case 8:return Component.translatable(MODID + ".text.numeral.9");
            case 9:return Component.translatable(MODID + ".text.numeral.10");
            case 10:return Component.translatable(MODID + ".text.numeral.11");
            case 11:return Component.translatable(MODID + ".text.numeral.12");
            case 12:return Component.translatable(MODID + ".text.numeral.13");
            case 13:return Component.translatable(MODID + ".text.numeral.14");
            case 14:return Component.translatable(MODID + ".text.numeral.15");
            case 15:return Component.translatable(MODID + ".text.numeral.16");
            case 16:return Component.translatable(MODID + ".text.numeral.17");
            case 17:return Component.translatable(MODID + ".text.numeral.18");
            case 18:return Component.translatable(MODID + ".text.numeral.19");
            case 19:return Component.translatable(MODID + ".text.numeral.20");
            default: return Component.translatable(MODID + ".text.numeral.1");
        }
    }
}
