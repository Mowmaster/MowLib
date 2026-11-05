package com.mowmaster.mowlib.reading;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

// The looked-at block, as the reading HUD hands it to IReadableBlock#read (on the client, once a tick). hit says where
// on the block the crosshair is (to pick a slot, a cell, a drawer...).
public record ReadingContext(Level level, Player player, BlockPos pos, BlockState state, @Nullable BlockEntity blockEntity, BlockHitResult hit)
{
}
