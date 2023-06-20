package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

public class MowLibRedstoneUtils
{
    public static int getRedstoneLevel(Level worldIn, BlockPos pos)
    {
        int hasItem=0;
        BlockEntity blockEntity = worldIn.getBlockEntity(pos);
        if(blockEntity instanceof MowLibBaseBlockEntity baseBlockEntity) {
        }

        return hasItem;
    }
}
