package com.mowmaster.mowlib.Items.WorkCards;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WorkCardLocations extends WorkCardBase implements ISelectablePoints {

    public WorkCardLocations(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return 2;
    }

    public static List<BlockPos> getPositionsInRangeOfUpgrade(ItemStack workCardStack, MowLibBaseBlockEntity baseBlockEntity, int rangeFromBlock) {
        if (workCardStack.getItem() instanceof WorkCardLocations workCard) {
            return WorkCardBase.readBlockPosListFromNBT(workCardStack).stream()
                .filter(blockPos -> workCard.selectedPointWithinRange(baseBlockEntity, blockPos, rangeFromBlock))
                .toList();
        } else {
            return List.of();
        }
    }
}
