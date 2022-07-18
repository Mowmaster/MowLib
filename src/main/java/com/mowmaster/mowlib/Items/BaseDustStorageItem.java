package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;

import net.minecraft.world.item.Item.Properties;
import net.minecraft.world.item.ItemStack;

public class BaseDustStorageItem extends Item {
    public BaseDustStorageItem(Properties p_41383_) {
        super(p_41383_);
    }

    public DustMagic getDustInItem(ItemStack stack)
    {
        return DustMagic.getDustMagicInItemStack(stack);
    }

    public void setDustMagicInItem(ItemStack stack, DustMagic dust)
    {
        DustMagic.setDustMagicInStack(stack, dust);
    }
}
