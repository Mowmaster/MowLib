package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.api.IColorable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class BaseDyeItem extends Item implements IColorable {
    public BaseDyeItem(Properties p_41383_) {
        super(p_41383_);
    }

    public static int getDyeColor(ItemStack dyeStack)
    {
        return MowLibColorReference.getColorFromItemStackInt(dyeStack);
    }

    public void setDyeColorInt(ItemStack dyeStack, int color)
    {
        MowLibColorReference.addColorToItemStack(dyeStack,color);
    }

    public void setDyeColorRGB(ItemStack dyeStack, int r, int g, int b)
    {
        MowLibColorReference.addColorToItemStack(dyeStack,r,g,b);
    }
}
