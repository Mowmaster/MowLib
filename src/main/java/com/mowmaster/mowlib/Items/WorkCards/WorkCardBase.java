package com.mowmaster.mowlib.Items.WorkCards;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlock;
import com.mowmaster.mowlib.Items.BaseUseInteractionItem;
import com.mowmaster.mowlib.MowLibUtils.MowLibComponentUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Networking.MowLibPacketHandler;
import com.mowmaster.mowlib.Networking.MowLibPacketParticles;
import com.mowmaster.mowlib.api.DefineLocations.ISelectableArea;
import com.mowmaster.mowlib.api.DefineLocations.ISelectablePoints;
import com.mowmaster.mowlib.api.DefineLocations.IWorkCard;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;

import javax.annotation.Nullable;
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

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {

        saveBlockPosToNBT(stack,10,context.getClickedPos());
        saveStringToNBT(stack,"mowlib_string_last_clicked_direction",context.getClickedFace().toString());
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
            MowLibMessageUtils.messagePopup(player,(added)?(ChatFormatting.WHITE):(ChatFormatting.BLACK),(added)?(MODID + ".workcard_blockpos_added"):(MODID + ".workcard_blockpos_removed"));
            MowLibPacketHandler.sendToNearby(level,player.getOnPos(),new MowLibPacketParticles(MowLibPacketParticles.EffectType.ANY_COLOR_CENTERED,atLocation.getX(),atLocation.getY()+1.0D,atLocation.getZ(),0,(added)?(200):(0),0));
        }
        return super.interactTargetBlock(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {

        ItemStack itemInHand = player.getItemInHand(hand);
        if(itemInHand.getItem() instanceof ISelectablePoints)
        {
            itemInHand.setTag(new CompoundTag());
            MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".workcard_blockpos_clear");
        }
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
                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".workcard_blockpos_second");
            }
            else if(!hasTwoPointsAlready)
            {
                saveBlockPosToNBT(itemInHand,1,atLocation);
                player.setItemInHand(hand,itemInHand);
                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".workcard_blockpos_first");
            }
        }
        return super.interactCrouchingTargetBlock(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactCrouchingTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {

        ItemStack itemInHand = player.getItemInHand(hand);
        if(itemInHand.getItem() instanceof ISelectableArea)
        {
            if (hasOneBlockPos(itemInHand))
            {
                itemInHand.setTag(new CompoundTag());
                MowLibMessageUtils.messagePopup(player,ChatFormatting.WHITE,MODID + ".workcard_blockpos_clear");
            }
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
                    MutableComponent base = Component.translatable(MODID + ".workcard_tooltip_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".workcard_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".text.separator.horizontal_rule_large.equals");
                    p_41423_.add(separator);

                    MutableComponent posOne = Component.translatable(MODID + ".workcard_tooltip_blockpos_one");
                    BlockPos blockPosOne = readBlockPosFromNBT(p_41421_,1);
                    MutableComponent posOnePos = Component.literal(blockPosOne.getX() + "x " + blockPosOne.getY() + "y " + blockPosOne.getZ()+ "z");
                    posOnePos.withStyle(ChatFormatting.GRAY);
                    posOne.append(Component.translatable(MODID + ".text.separator.colon_space"));
                    posOne.append(posOnePos);

                    p_41423_.add(posOne);

                    MutableComponent posTwo = Component.translatable(MODID + ".workcard_tooltip_blockpos_two");
                    BlockPos blockPosTwo = readBlockPosFromNBT(p_41421_,2);
                    MutableComponent posTwoPos = Component.literal(blockPosTwo.getX() + "x " + blockPosTwo.getY() + "y " + blockPosTwo.getZ()+ "z");
                    posTwoPos.withStyle(ChatFormatting.GRAY);
                    posTwo.append(Component.translatable(MODID + ".text.separator.colon_space"));
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
                    MutableComponent base = Component.translatable(MODID + ".workcard_tooltip_shift");
                    base.withStyle(ChatFormatting.WHITE);
                    p_41423_.add(base);
                } else {
                    MutableComponent posTitle = Component.translatable(MODID + ".workcard_tooltip_blockpos_title");
                    posTitle.withStyle(ChatFormatting.GOLD);
                    p_41423_.add(posTitle);

                    //Separator
                    MutableComponent separator = Component.translatable(MODID + ".text.separator.horizontal_rule_large.equals");
                    p_41423_.add(separator);

                    for (BlockPos pos : getList) {
                        MutableComponent formattedPos = MowLibComponentUtils.getBlockPosFormatted(pos);
                        formattedPos.withStyle(ChatFormatting.GRAY);
                        p_41423_.add(formattedPos);
                    }
                }
            }
        }
    }
}
