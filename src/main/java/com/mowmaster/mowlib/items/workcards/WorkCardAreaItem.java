package com.mowmaster.mowlib.items.workcards;

import com.mowmaster.mowlib.workcard.IWorkCardHolder;
import com.mowmaster.mowlib.workcard.WorkCardType;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

// Two corners of a box (1.20's WorkCardArea). The first click sets one corner, the second click the other.
public class WorkCardAreaItem extends WorkCardItem
{
    public WorkCardAreaItem(Properties properties)
    {
        super(properties);
    }

    @Override
    public WorkCardType getWorkCardType()
    {
        return WorkCardType.AREA;
    }

    @Override
    protected Component select(Level level, ItemStack card, BlockPos pos)
    {
        List<BlockPos> corners = new ArrayList<>(getStoredPositions(card));
        if (corners.size() >= 2) return Component.translatable(MODID + ".workcard_blockpos_full");
        corners.add(pos);
        setStoredPositions(card, corners);
        return Component.translatable(MODID + (corners.size() == 1 ? ".workcard_blockpos_first" : ".workcard_blockpos_second"));
    }

    // The box between the corners, as min/max block positions, if both are set and in range (and no bigger than
    // the holder's getMaxAreaSize, if it has one)
    private static Optional<BlockPos[]> getCorners(ItemStack card, IWorkCardHolder holder, int range)
    {
        List<BlockPos> corners = getStoredPositions(card);
        if (corners.size() < 2) return Optional.empty();
        BlockPos first = corners.get(0);
        BlockPos second = corners.get(1);
        if (!inRange(holder, first, range) || !inRange(holder, second, range)) return Optional.empty();
        BlockPos min = BlockPos.min(first, second);
        BlockPos max = BlockPos.max(first, second);
        int size = holder.getMaxAreaSize();
        if (size >= 0 && (max.getX() - min.getX() > size || max.getY() - min.getY() > size || max.getZ() - min.getZ() > size)) return Optional.empty();
        return Optional.of(new BlockPos[]{min, max});
    }

    // Every block in the box. When the box goes below the holder, each column is listed top-down, so breakers and
    // quarries dig downwards (same order as 1.20).
    @Override
    public List<BlockPos> getPositions(ItemStack card, IWorkCardHolder holder, int range)
    {
        return getCorners(card, holder, range).map(corners -> {
            BlockPos min = corners[0];
            BlockPos max = corners[1];
            List<BlockPos> positions = new ArrayList<>();
            if (min.getY() < holder.getBlockPos().getY())
            {
                for (int x = max.getX(); x >= min.getX(); x--)
                    for (int z = max.getZ(); z >= min.getZ(); z--)
                        for (int y = max.getY(); y >= min.getY(); y--)
                            positions.add(new BlockPos(x, y, z));
            }
            else
            {
                for (int x = min.getX(); x <= max.getX(); x++)
                    for (int z = min.getZ(); z <= max.getZ(); z++)
                        for (int y = min.getY(); y <= max.getY(); y++)
                            positions.add(new BlockPos(x, y, z));
            }
            return positions;
        }).orElse(List.of());
    }

    // The whole box, including the far blocks' full size, for finding entities
    @Override
    public Optional<AABB> getArea(ItemStack card, IWorkCardHolder holder, int range)
    {
        return getCorners(card, holder, range).map(corners -> AABB.encapsulatingFullBlocks(corners[0], corners[1]));
    }
}
