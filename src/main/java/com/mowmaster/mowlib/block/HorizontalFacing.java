package com.mowmaster.mowlib.block;

import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;

// For blocks that face one of the four horizontal directions with a FACING property. Blocks that already extend
// another class can't share a base class, so these are static helpers; call them from the block's own overrides:
//
//   public static final EnumProperty<Direction> FACING = HorizontalFacing.FACING;
//   @Override protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) { builder.add(FACING); }
//   @Override public BlockState getStateForPlacement(BlockPlaceContext context) { return HorizontalFacing.towardsPlayer(defaultBlockState(), context); }
//   @Override protected BlockState rotate(BlockState state, Rotation rotation) { return HorizontalFacing.rotate(state, rotation); }
//   @Override protected BlockState mirror(BlockState state, Mirror mirror) { return HorizontalFacing.mirror(state, mirror); }
//
// Rotate and mirror are what structure blocks and other mods' rotating tools use, so getting them right matters even
// if the block never turns itself.
public final class HorizontalFacing
{
    private HorizontalFacing() {}

    // Vanilla's own horizontal facing property (north, east, south, west)
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;

    // Its front towards the player placing it
    public static BlockState towardsPlayer(BlockState state, BlockPlaceContext context)
    {
        return state.setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    // Its back towards the player placing it (the front faces where they look)
    public static BlockState awayFromPlayer(BlockState state, BlockPlaceContext context)
    {
        return state.setValue(FACING, context.getHorizontalDirection());
    }

    public static BlockState rotate(BlockState state, Rotation rotation)
    {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    public static BlockState mirror(BlockState state, Mirror mirror)
    {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    // Clockwise quarter turns from north (north 0, east 1, south 2, west 3): what a blockstate's "y": 90 * n turns
    public static int quarterTurns(Direction facing)
    {
        return switch (facing)
        {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
    }
}
