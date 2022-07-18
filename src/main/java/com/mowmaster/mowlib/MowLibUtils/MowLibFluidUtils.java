package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;

public class MowLibFluidUtils
{
    public static void dropLiquidsInWorld(Level level, BlockPos pos, IFluidHandler fluids) {
        FluidStack fluidStack = fluids.getFluidInTank(0).copy();
        Item item = fluidStack.getFluid().getBucket();

        int x = -1;
        int z = -1;
        int y = 0;
        while (fluidStack.getAmount()>=1000)
        {
            if(item instanceof BucketItem)
            {
                BucketItem bucketItem = (BucketItem) item;
                BlockState state = level.getBlockState(pos.offset(x,y,z));
                if(state.getBlock().equals(Blocks.AIR))
                {
                    if(bucketItem.emptyContents(null,level,pos.offset(x,y,z),null))fluidStack.grow(-1000);
                }

                if(x>=1 && z>=1)
                {
                    y+=1;
                    x=-1;
                    z=-1;
                }

                if(x>=1)
                {
                    x=-1;
                    z+=1;
                }

                x+=1;
            }

        }
    }
}
