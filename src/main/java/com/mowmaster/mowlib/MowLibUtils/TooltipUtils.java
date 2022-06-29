package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;

//https://www.youtube.com/watch?v=uOAeHJtZMZM
public class TooltipUtils
{

    public static void addTooltipMessage(List<Component> componentList, ItemStack stack, MutableComponent translatableComponent)
    {
        MutableComponent base = translatableComponent;
        componentList.add(base);
    }

    public static void addTooltipMessageWithStyle(List<Component> componentList, MutableComponent translatableComponent, ChatFormatting chatFormatting)
    {
        MutableComponent base = translatableComponent;
        base.withStyle(chatFormatting);
        componentList.add(base);
    }

    public static void addTooltipMessageWithStyle(List<Component> componentList, String localizationString, ChatFormatting chatFormatting)
    {
        MutableComponent base = Component.translatable(localizationString);
        base.withStyle(chatFormatting);
        componentList.add(base);
    }

    public static void addTooltipShiftMessage(String MODID, List<Component> componentList, ItemStack stack, MutableComponent translatableComponent)
    {
        if(!Screen.hasShiftDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            MutableComponent base = translatableComponent;
            componentList.add(base);
        }
    }

    public static void addTooltipShiftMessageWithStyle(String MODID, List<Component> componentList, String localizationString, ChatFormatting chatFormatting)
    {
        if(!Screen.hasShiftDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            if(localizationString.contains(MODID))
            {
                MutableComponent base = Component.translatable(localizationString);
                base.withStyle(chatFormatting);
                componentList.add(base);
            }
            else
            {
                MutableComponent base = Component.literal(localizationString);
                base.withStyle(chatFormatting);
                componentList.add(base);
            }
        }
    }

    public static void addTooltipShiftMessageMultiWithStyle(String MODID, List<Component> componentList, List<String> localizationString, List<ChatFormatting> chatFormatting)
    {
        if(!Screen.hasShiftDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            for(int i=0;i<localizationString.size();i++)
            {
                if(localizationString.get(i).contains(MODID))
                {
                    MutableComponent base = Component.translatable(localizationString.get(i));
                    base.withStyle((chatFormatting.size()>=i)?(chatFormatting.get(i)):(chatFormatting.get(0)));
                    componentList.add(base);
                }
                else
                {
                    MutableComponent base = Component.literal(localizationString.get(i));
                    base.withStyle((chatFormatting.size()>=i)?(chatFormatting.get(i)):(chatFormatting.get(0)));
                    componentList.add(base);
                }
            }
        }
    }

    public static void addTooltipShiftMessageWithStyle(String MODID, List<Component> componentList, MutableComponent translatableComponent, ChatFormatting chatFormatting)
    {
        if(!Screen.hasShiftDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_shift");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            MutableComponent base = translatableComponent;
            base.withStyle(chatFormatting);
            componentList.add(base);
        }
    }

    public static void addTooltipAltMessage(String MODID, List<Component> componentList, ItemStack stack, MutableComponent translatableComponent)
    {
        if(!Screen.hasAltDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            MutableComponent base = translatableComponent;
            componentList.add(base);
        }
    }

    public static void addTooltipAltMessageWithStyle(String MODID, List<Component> componentList, String localizationString, ChatFormatting chatFormatting)
    {
        if(!Screen.hasAltDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            if(localizationString.contains(MODID))
            {
                MutableComponent base = Component.translatable(localizationString);
                base.withStyle(chatFormatting);
                componentList.add(base);
            }
            else
            {
                MutableComponent base = Component.literal(localizationString);
                base.withStyle(chatFormatting);
                componentList.add(base);
            }
        }
    }

    public static void addTooltipAltMessageWithStyle(String MODID, List<Component> componentList, MutableComponent translatableComponent, ChatFormatting chatFormatting)
    {
        if(!Screen.hasAltDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            MutableComponent base = translatableComponent;
            base.withStyle(chatFormatting);
            componentList.add(base);
        }
    }

    public static void addTooltipAltMessageMultiWithStyle(String MODID, List<Component> componentList, List<String> localizationString, List<ChatFormatting> chatFormatting)
    {
        if(!Screen.hasAltDown())
        {
            MutableComponent base = Component.translatable(MODID + ".description_alt");
            base.withStyle(ChatFormatting.WHITE);
            componentList.add(base);
        }
        else {
            for(int i=0;i<localizationString.size();i++)
            {
                if(localizationString.get(i).contains(MODID))
                {
                    MutableComponent base = Component.translatable(localizationString.get(i));
                    base.withStyle((chatFormatting.size()>=i)?(chatFormatting.get(i)):(chatFormatting.get(0)));
                    componentList.add(base);
                }
                else
                {
                    MutableComponent base = Component.literal(localizationString.get(i));
                    base.withStyle((chatFormatting.size()>=i)?(chatFormatting.get(i)):(chatFormatting.get(0)));
                    componentList.add(base);
                }

            }
        }
    }

    public static String getEnchantRomanNumeral(int value)
    {
        switch(value)
        {
            case 0:return "I";
            case 1:return "II";
            case 2:return "III";
            case 3:return "IV";
            case 4:return "V";
            case 5:return "VI";
            case 6:return "VII";
            case 7:return "VIII";
            case 8:return "IX";
            case 9:return "X";
            case 10:return "XI";
            case 11:return "XII";
            case 12:return "XIII";
            case 13:return "XIV";
            case 14:return "XV";
            case 15:return "XVI";
            case 16:return "XVII";
            case 17:return "XVIII";
            case 18:return "XIV";
            case 19:return "XX";
            default: return "I";
        }
    }
}
