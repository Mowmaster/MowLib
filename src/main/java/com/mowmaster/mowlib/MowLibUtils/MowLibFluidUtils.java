package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.CapabilityFluidHandler;
import net.minecraftforge.fluids.capability.IFluidHandler;

import java.util.List;

public class MowLibFluidUtils
{
    public static LazyOptional<IFluidHandler> findFluidHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IFluidHandler> cap = neighbourTile.getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IFluidHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IFluidHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IFluidHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityFluidHandler.FLUID_HANDLER_CAPABILITY);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

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

    public static FluidStack getFluidStackFromItemStack(ItemStack stackIn)
    {
        if(stackIn.getItem() instanceof BucketItem bucket)
        {
            Fluid bucketFluid = bucket.getFluid();
            return new FluidStack(bucketFluid,1000);
        }

        return FluidStack.EMPTY;
    }
}
