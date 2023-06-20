package com.mowmaster.mowlib.Items.WorkCards;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.api.DefineLocations.ISelectablePoints;
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
        return MowLibBlockPosUtils.readBlockPosListFromNBT(workCardStack).stream()
                .filter(blockPos -> MowLibBlockPosUtils.selectedPointWithinRange(baseBlockEntity, blockPos, rangeFromBlock))
                .toList();
    }
}
