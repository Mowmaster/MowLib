package com.mowmaster.mowlib.BlockEntities;

import com.mowmaster.mowlib.Blocks.BaseBlocks.BaseColoredBlock;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;


public class MowLibBaseBlock extends BaseColoredBlock implements EntityBlock
{
    public MowLibBaseBlock(Properties p_152915_) {
        super(p_152915_);
        this.registerDefaultState(MowLibColorReference.addColorToBlockState(this.defaultBlockState(),MowLibColorReference.DEFAULTCOLOR));
    }

    @Override
    public void neighborChanged(BlockState p_60509_, Level p_60510_, BlockPos p_60511_, Block p_60512_, BlockPos p_60513_, boolean p_60514_) {

        if(!p_60510_.isClientSide())
        {
            if(p_60513_.equals(MowLibBlockPosUtils.getPosBelowBlockEntity(p_60509_, p_60511_, 1)))
            {
                if(p_60510_.getBlockEntity(p_60511_) instanceof MowLibBaseBlockEntity baseBlockEntity)
                {
                    baseBlockEntity.actionOnNeighborBelowChange(MowLibBlockPosUtils.getPosBelowBlockEntity(p_60509_, p_60511_, 1));
                }
            }
        }
        super.neighborChanged(p_60509_, p_60510_, p_60511_, p_60512_, p_60513_, p_60514_);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return null;
    }
}
