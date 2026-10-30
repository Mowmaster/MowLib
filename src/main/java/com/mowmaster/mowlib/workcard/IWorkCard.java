package com.mowmaster.mowlib.workcard;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Optional;

// Implemented by work card items (1.20's MowLib IWorkCard). A machine holds one work card and reads its positions
// through these methods, passing itself as the holder.
public interface IWorkCard
{
    WorkCardType getWorkCardType();

    // The card's positions that are within range of the holder (range is per axis). Empty if none are.
    List<BlockPos> getPositions(ItemStack card, IWorkCardHolder holder, int range);

    // For area cards: the selected box as entity search bounds, if both corners are set and in range
    default Optional<AABB> getArea(ItemStack card, IWorkCardHolder holder, int range)
    {
        return Optional.empty();
    }
}
