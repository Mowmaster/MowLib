package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public class MowLibBlockPosUtils
{
    public BlockPos getPosOfBlockBelow(Level level, BlockPos posOfPedestal, int numBelow)
    {
        BlockState state = level.getBlockState(posOfPedestal);

        Direction enumfacing = (state.hasProperty(BlockStateProperties.FACING))?(state.getValue(BlockStateProperties.FACING)):(Direction.UP);
        BlockPos blockBelow = posOfPedestal;
        switch (enumfacing)
        {
            case UP:
                return blockBelow.offset(0,-numBelow,0);
            case DOWN:
                return blockBelow.offset(0,numBelow,0);
            case NORTH:
                return blockBelow.offset(0,0,numBelow);
            case SOUTH:
                return blockBelow.offset(0,0,-numBelow);
            case EAST:
                return blockBelow.offset(-numBelow,0,0);
            case WEST:
                return blockBelow.offset(numBelow,0,0);
            default:
                return blockBelow;
        }
    }
}
