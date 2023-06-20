package com.mowmaster.mowlib.Items.WorkCards;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlock;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.Items.BaseUseInteractionItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.mowlib.api.DefineLocations.ISelectableArea;
import com.mowmaster.mowlib.api.DefineLocations.ISelectablePoints;
import com.mowmaster.mowlib.api.DefineLocations.IWorkCard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils.*;
import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class WorkCardBase extends BaseUseInteractionItem implements IWorkCard
{

    public WorkCardBase(Properties p_41383_) {
        super(p_41383_);
    }

    public int getWorkCardType()
    {
        return -1;
    }

    public static void saveBlockPosListCustomToNBT(ItemStack upgrade, String tagGenericName, List<BlockPos> posListToSave)
    {
        CompoundTag compound = new CompoundTag();
        if(upgrade.hasTag())
        {
            compound = upgrade.getTag();
        }
        List<Integer> storedX = new ArrayList<Integer>();
        List<Integer> storedY = new ArrayList<Integer>();
        List<Integer> storedZ = new ArrayList<Integer>();

        for(int i=0;i<posListToSave.size();i++)
        {
            storedX.add(posListToSave.get(i).getX());
            storedY.add(posListToSave.get(i).getY());
            storedZ.add(posListToSave.get(i).getZ());
        }

        compound.putIntArray(MODID+tagGenericName+"_X",storedX);
        compound.putIntArray(MODID+tagGenericName+"_Y",storedY);
        compound.putIntArray(MODID+tagGenericName+"_Z",storedZ);
        upgrade.setTag(compound);
    }

    public static List<BlockPos> readBlockPosListCustomFromNBT(ItemStack upgrade, String tagGenericName) {
        List<BlockPos> posList = new ArrayList<>();
        if(upgrade.hasTag())
        {
            String tagX = MODID+tagGenericName+"_X";
            String tagY = MODID+tagGenericName+"_Y";
            String tagZ = MODID+tagGenericName+"_Z";
            CompoundTag getCompound = upgrade.getTag();
            if(upgrade.getTag().contains(tagX) && upgrade.getTag().contains(tagY) && upgrade.getTag().contains(tagZ))
            {
                int[] storedIX = getCompound.getIntArray(tagX);
                int[] storedIY = getCompound.getIntArray(tagY);
                int[] storedIZ = getCompound.getIntArray(tagZ);

                for(int i=0;i<storedIX.length;i++)
                {
                    BlockPos gotPos = new BlockPos(storedIX[i],storedIY[i],storedIZ[i]);
                    posList.add(gotPos);
                }
            }
        }
        return posList;
    }

    public void removeBlockListCustomNBTTags(ItemStack upgrade, String tagGenericName)
    {
        String tagX = MODID+tagGenericName+"_X";
        String tagY = MODID+tagGenericName+"_Y";
        String tagZ = MODID+tagGenericName+"_Z";
        CompoundTag getTags = upgrade.getTag();
        if(getTags.contains(tagX))getTags.remove(tagX);
        if(getTags.contains(tagY))getTags.remove(tagY);
        if(getTags.contains(tagZ))getTags.remove(tagZ);
        upgrade.setTag(getTags);
    }

    public boolean hasBlockListCustomNBTTags(ItemStack upgrade, String tagGenericName)
    {
        String tagX = MODID+tagGenericName+"_X";
        String tagY = MODID+tagGenericName+"_Y";
        String tagZ = MODID+tagGenericName+"_Z";
        CompoundTag getTags = upgrade.getTag();

        return getTags.contains(tagX) && getTags.contains(tagY) && getTags.contains(tagZ);
    }




    public static BlockPos getExistingSingleBlockPos(ItemStack stack) {
        return (!readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO))?(readBlockPosFromNBT(stack,1)):(readBlockPosFromNBT(stack,2));
    }


    public boolean isNewBlockPosSmallerThanExisting(ItemStack stack, BlockPos posTwo) {
        BlockPos posOne = getExistingSingleBlockPos(stack);
        BlockPos toCompare = new BlockPos(Math.min(posOne.getX(), posTwo.getX()),Math.min(posOne.getY(), posTwo.getY()),Math.min(posOne.getZ(), posTwo.getZ()));

        return (posTwo.equals(toCompare))?(true):(false);
    }


    public boolean hasTwoPointsSelected(ItemStack stack)
    {
        return !readBlockPosFromNBT(stack,1).equals(BlockPos.ZERO) && !readBlockPosFromNBT(stack,2).equals(BlockPos.ZERO);
    }






    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        saveBlockPosToNBT(stack,10,context.getClickedPos());
        saveStringToNBT(stack,"_string_last_clicked_direction",context.getClickedFace().toString());
        return super.onItemUseFirst(stack, context);
    }

    @Override
    public InteractionResultHolder interactTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        ItemStack itemInHand = player.getItemInHand(hand);
        BlockPos atLocation = readBlockPosFromNBT(itemInHand,10);
        if(itemInHand.getItem() instanceof ISelectablePoints)
        {
            if(getWorkCardType() == 3)
            {
                if(!(level.getBlockState(atLocation).getBlock() instanceof MowLibBaseBlock))
                    return InteractionResultHolder.fail(player.getItemInHand(hand));
            }
            boolean added = addBlockPosToList(itemInHand,atLocation);
            player.setItemInHand(hand,itemInHand);
            MowLibMessageUtils.messagePopup(player,(added)?(ChatFormatting.WHITE):(ChatFormatting.BLACK),(added)?(MODID + ".upgrade_blockpos_added"):(MODID + ".upgrade_blockpos_removed"));
            MowLibPacketHandler.sendToNearby(level,player.getOnPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,atLocation.getX(),atLocation.getY()+1.0D,atLocation.getZ(),0,(added)?(200):(0),0));
        }
        return super.interactTargetBlock(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {

        ItemStack itemInHand = player.getItemInHand(hand);
        itemInHand.setTag(new CompoundTag());
        MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_clear");
        return super.interactTargetAir(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactCrouchingTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {

        ItemStack itemInHand = player.getItemInHand(hand);
        BlockPos atLocation = readBlockPosFromNBT(itemInHand,10);
        if(itemInHand.getItem() instanceof ISelectableArea)
        {
            Boolean hasOnePointAlready = hasOneBlockPos(itemInHand);
            Boolean hasTwoPointsAlready = hasTwoPointsSelected(itemInHand);

            if(hasOnePointAlready && !hasTwoPointsAlready)
            {
                saveBlockPosToNBT(itemInHand,2,atLocation);
                player.setItemInHand(hand,itemInHand);
                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_second");
            }
            else if(!hasTwoPointsAlready)
            {
                saveBlockPosToNBT(itemInHand,1,atLocation);
                player.setItemInHand(hand,itemInHand);
                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_first");
            }
        }
        return super.interactCrouchingTargetBlock(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactCrouchingTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {

        ItemStack itemInHand = player.getItemInHand(hand);
        if (hasOneBlockPos(itemInHand))
        {
            itemInHand.setTag(new CompoundTag());
            MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".upgrade_blockpos_clear");
        }

        return super.interactCrouchingTargetAir(level, player, hand, itemStackInHand, result);
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> p_41423_, TooltipFlag p_41424_) {
        super.appendHoverText(p_41421_, p_41422_, p_41423_, p_41424_);

        /*=====================================
        =======================================
        =====================================*/

        if(Screen.hasShiftDown() && Screen.hasAltDown())
        {
            //Add a new Line if both are present
            p_41423_.add(Component.literal(""));
        }

        if(p_41421_.getItem() instanceof WorkCardBase && p_41421_.getItem() instanceof ISelectableArea)
        {
            if(hasOneBlockPos(p_41421_))
            {
                if (!Screen.hasShiftDown()) {
                    MutableComponent base = Component.translatable(MODID + ".upgrade_description_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    MutableComponent posOne = Component.translatable(MODID + ".upgrade_tooltip_blockpos_one");
                    BlockPos blockPosOne = readBlockPosFromNBT(p_41421_,1);
                    MutableComponent posOnePos = Component.literal(blockPosOne.getX() + "x " + blockPosOne.getY() + "y " + blockPosOne.getZ()+ "z");
                    posOnePos.withStyle(ChatFormatting.GRAY);
                    posOne.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posOne.append(posOnePos);

                    p_41423_.add(posOne);

                    MutableComponent posTwo = Component.translatable(MODID + ".upgrade_tooltip_blockpos_two");
                    BlockPos blockPosTwo = readBlockPosFromNBT(p_41421_,2);
                    MutableComponent posTwoPos = Component.literal(blockPosTwo.getX() + "x " + blockPosTwo.getY() + "y " + blockPosTwo.getZ()+ "z");
                    posTwoPos.withStyle(ChatFormatting.GRAY);
                    posTwo.append(Component.translatable(MODID + ".upgrade_tooltip_separator"));
                    posTwo.append(posTwoPos);
                    p_41423_.add(posTwo);
                }
            }
        }

        if(p_41421_.getItem() instanceof WorkCardBase && p_41421_.getItem() instanceof ISelectablePoints && !hasTwoPointsSelected(p_41421_))
        {
            List<BlockPos> getList = readBlockPosListFromNBT(p_41421_);
            if(getList.size()>0)
            {
                if (!Screen.hasShiftDown()) {
                    MutableComponent base = Component.translatable(MODID + ".upgrade_description_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".upgrade_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".tooltip_separator");
                    p_41423_.add(separator);

                    for (BlockPos pos : getList) {
                        MutableComponent posOnePos = Component.literal(pos.getX() + "x " + pos.getY() + "y " + pos.getZ()+ "z");
                        posOnePos.withStyle(ChatFormatting.GRAY);
                        p_41423_.add(posOnePos);
                    }
                }
            }
        }
    }
}
