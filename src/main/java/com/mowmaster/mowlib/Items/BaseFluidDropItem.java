package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.MowLibUtils.MowLibFluidUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibXpUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandlerItem;
import org.jetbrains.annotations.NotNull;

public class BaseFluidDropItem extends Item implements IFluidHandlerItem {
    public BaseFluidDropItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public @NotNull ItemStack getContainer() {
        return null;
    }

    @Override
    public int getTanks() {
        return 0;
    }

    @Override
    public @NotNull FluidStack getFluidInTank(int tank) {
        return null;
    }

    @Override
    public int getTankCapacity(int tank) {
        return 0;
    }

    @Override
    public boolean isFluidValid(int tank, @NotNull FluidStack stack) {
        return false;
    }

    @Override
    public int fill(FluidStack resource, FluidAction action) {
        return 0;
    }

    @Override
    public @NotNull FluidStack drain(FluidStack resource, FluidAction action) {
        return null;
    }

    @Override
    public @NotNull FluidStack drain(int maxDrain, FluidAction action) {
        return null;
    }


    public int getItemVariant()
    {
        int tank = 0;
        if(getFluidInTank(tank).getAmount()>0)
        {
            double renderDevider = (double)(100.0 * (getFluidInTank(tank).getAmount()/getTankCapacity(tank)));
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
            MowLibFluidUtils.dropLiquidsInWorld(p_41405_,p_41406_.getOnPos(),this);
        }
    }
}
