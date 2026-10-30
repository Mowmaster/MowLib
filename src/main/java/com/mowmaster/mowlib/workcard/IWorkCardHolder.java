package com.mowmaster.mowlib.workcard;

import net.minecraft.core.BlockPos;

// What a work card asks of the machine it's in (a block entity, usually: getBlockPos comes from BlockEntity).
public interface IWorkCardHolder
{
    BlockPos getBlockPos();

    // Within range of the machine on every axis (a cube around it)
    default boolean isInRange(BlockPos target, int range)
    {
        BlockPos pos = getBlockPos();
        return Math.abs(target.getX() - pos.getX()) <= range
                && Math.abs(target.getY() - pos.getY()) <= range
                && Math.abs(target.getZ() - pos.getZ()) <= range;
    }

    // The biggest an area card's box may be along each axis (in blocks between the corners), or -1 for no limit
    default int getMaxAreaSize()
    {
        return -1;
    }
}
