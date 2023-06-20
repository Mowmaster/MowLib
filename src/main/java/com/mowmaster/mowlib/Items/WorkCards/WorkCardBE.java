package com.mowmaster.mowlib.Items.WorkCards;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.api.DefineLocations.ISelectablePoints;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WorkCardBE extends WorkCardBase implements ISelectablePoints {

    public WorkCardBE(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return 3;
    }

    // This returns all the stored locations regardless of whether they are currently pedestals. As typical uses of this
    // list is to cache it once for all future uses, doing so would be not only unnecessary but give an incorrect sense
    // that upgrades don't have to verify the list every time it is used (as the blocks at those positions could change
    // at any point after this is cached).
    public static List<BlockPos> getPositionsInRangeOfUpgrade(ItemStack workCardStack, MowLibBaseBlockEntity baseBlockEntity, int rangeFromBlock) {
        return MowLibBlockPosUtils.readBlockPosListFromNBT(workCardStack).stream()
                .filter(blockPos -> MowLibBlockPosUtils.selectedPointWithinRange(baseBlockEntity, blockPos, rangeFromBlock))
                .toList();
    }
}
