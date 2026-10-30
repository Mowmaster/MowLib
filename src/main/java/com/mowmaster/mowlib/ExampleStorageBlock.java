package com.mowmaster.mowlib;

import com.mowmaster.mowlib.storage.CBBulkStorageBlock;
import com.mowmaster.mowlib.tint.IColorableBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.common.Tags;
import org.jetbrains.annotations.NotNull;

public class ExampleStorageBlock extends CBBulkStorageBlock
{
    public static final int CAPACITY = 1;
    public ExampleStorageBlock(Properties properties)
    {
        super(properties, CAPACITY, Tags.Items.INGOTS);
    }

    @Override
    protected @NotNull InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
                                                   Player player, InteractionHand hand, BlockHitResult hitResult)
    {
        return tryDyeBlock(stack, state, level, pos, player, hitResult);
    }
}