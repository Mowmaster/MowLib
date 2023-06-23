package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlockEntity;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardArea;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBE;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBase;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardLocations;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.saveBlockPosListCustomToNBT;
import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MowLibWorkCardUtil
{
    public static void resetCachedValidWorkCardPositions(String modid, ItemStack upgrade) {
        removeBlockListCustomNBTTags(modid, "_validlist",upgrade);
    }

    public static List<BlockPos> getValidWorkCardPositions(MowLibBaseFilterableBlockEntity filterableBlockEntity, ItemStack upgrade, int workCardType, int getUpgradeRange, String modid) {
        //ItemStack upgrade = pedestal.getCoinOnPedestal();
        List<BlockPos> cached = readBlockPosListCustomFromNBT(modid, "_validlist",upgrade);
        if (cached.size() == 0) {
            // Optimization to construct the validlist only once. The NBT tag should be reset when the WorkCard/Upgrade
            // is removed (as that is the only way to invalidate the cached list) by calling `resetCachedValidWorkCardPositions`.
            if (!hasBlockListCustomNBTTags(modid, "_validlist",upgrade) && filterableBlockEntity.hasWorkCard()) {
                ItemStack workCardItemStack = filterableBlockEntity.getWorkCardInPedestal();
                if (workCardItemStack.getItem() instanceof WorkCardBase baseCard) {
                    int supportedWorkCardTypesForThisUpgrade = workCardType;
                    int insertedWorkCardType = baseCard.getWorkCardType();
                    if (
                            insertedWorkCardType == supportedWorkCardTypesForThisUpgrade || // exact match
                                    (supportedWorkCardTypesForThisUpgrade == 0 && (insertedWorkCardType == 1 || insertedWorkCardType == 2)) // match for the "either" area or locations type
                    ) {
                        cached = switch (baseCard.getWorkCardType()) {
                            case 1 -> WorkCardArea.getPositionsInRangeOfUpgrade(workCardItemStack, filterableBlockEntity, getUpgradeRange);
                            case 2 -> WorkCardLocations.getPositionsInRangeOfUpgrade(workCardItemStack, filterableBlockEntity, getUpgradeRange);
                            case 3 -> WorkCardBE.getPositionsInRangeOfUpgrade(workCardItemStack, filterableBlockEntity, getUpgradeRange);
                            default -> List.of();
                        };
                    }
                }
                saveBlockPosListCustomToNBT(modid, "_validlist",upgrade, cached);
            }
        }
        return cached;
    }
}
