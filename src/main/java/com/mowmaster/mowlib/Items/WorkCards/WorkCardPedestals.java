package com.mowmaster.mowlib.Items.WorkCards;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class WorkCardPedestals extends WorkCardBase implements ISelectablePoints {

    //Locations in PedestalBlock class
    //Ln486
    //Ln883

    public WorkCardPedestals(Properties p_41383_) {
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
        if (workCardStack.getItem() instanceof WorkCardPedestals workCard) {
            return WorkCardBase.readBlockPosListFromNBT(workCardStack).stream()
                    .filter(blockPos -> workCard.selectedPointWithinRange(baseBlockEntity, blockPos, rangeFromBlock))
                    .toList();
        } else {
            return List.of();
        }
    }
}
