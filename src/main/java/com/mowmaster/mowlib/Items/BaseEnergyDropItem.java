package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.MowLibUtils.MowLibEnergyUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibFluidUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.energy.IEnergyStorage;

public class BaseEnergyDropItem extends Item implements IEnergyStorage
{
    public BaseEnergyDropItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public int receiveEnergy(int maxReceive, boolean simulate) {
        return 0;
    }

    @Override
    public int extractEnergy(int maxExtract, boolean simulate) {
        return 0;
    }

    @Override
    public int getEnergyStored() {
        return 0;
    }

    @Override
    public int getMaxEnergyStored() {
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
        if(getEnergyStored()>0)
        {
            double renderDevider = (double)(100.0 * (getEnergyStored()/getMaxEnergyStored()));
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
            MowLibEnergyUtils.removeEnergy(p_41405_,p_41406_.getOnPos(),this);
        }
    }
}
