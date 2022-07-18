package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class BaseXpDropItem extends Item implements IExperienceStorage {
    public BaseXpDropItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public int receiveExperience(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractExperience(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getExperienceStored() {
        return 0;
    }

    @Override
    public int getMaxExperienceStored() {
        return 0;
    }

    @Override
    public boolean canExtract() {
        return false;
    }

    @Override
    public boolean canReceive() {
        return false;
    }

    public int getItemVariant()
    {
        if(getExperienceStored()>0)
        {
            double renderDevider = (double)(100.0 * (getExperienceStored()/getMaxExperienceStored()));
            if(renderDevider<=25.0)return 0;
            else if(renderDevider<=50.0)return 1;
            else if(renderDevider<=75.0)return 2;
            else if(renderDevider>=100.0)return 3;
        }

        return 0;
    }

    @Override
    public void inventoryTick(ItemStack p_41404_, Level p_41405_, Entity p_41406_, int p_41407_, boolean p_41408_) {
        super.inventoryTick(p_41404_, p_41405_, p_41406_, p_41407_, p_41408_);
        if(p_41406_ instanceof Player player && player.isCreative())return;
        else
        {
            MowLibXpUtils.dropXPInWorld(p_41405_,p_41406_.getOnPos(),this);
        }
    }
}
