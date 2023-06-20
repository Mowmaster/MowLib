package com.mowmaster.mowlib.Items.Tools;

import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import com.mowmaster.mowlib.api.Tools.IMowLibTool;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.HitResult;

public class DevTool extends BaseTool implements IMowLibTool
{
    public DevTool(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder interactTargetAir(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {

        ItemStack stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);
        if(stackInOffHand.hasTag())
        {
            MowLibMessageUtils.messagePlayerChatText(player, ChatFormatting.GOLD, stackInOffHand.getTag().toString());
        }
        return super.interactTargetAir(level, player, hand, itemStackInHand, result);
    }

    @Override
    public InteractionResultHolder interactTargetBlock(Level level, Player player, InteractionHand hand, ItemStack itemStackInHand, HitResult result) {
        BlockPos pos = new BlockPos((int)result.getLocation().x,(int)result.getLocation().y,(int)result.getLocation().z);
        BlockState statedHit = level.getBlockState(pos);
        ItemStack stackInOffHand = player.getItemInHand(InteractionHand.OFF_HAND);

        if(stackInOffHand.getItem().equals(Items.HONEYCOMB))
        {
            if (statedHit.is(BlockTags.BEEHIVES, (p_202454_) -> {
                return p_202454_.hasProperty(BeehiveBlock.HONEY_LEVEL) && p_202454_.getBlock() instanceof BeehiveBlock;
            })) {
                int i = statedHit.getValue(BeehiveBlock.HONEY_LEVEL);
                if (i < 5) {
                    level.setBlockAndUpdate(pos,statedHit.setValue(BeehiveBlock.HONEY_LEVEL,5));
                }
            }
        }

        return super.interactTargetBlock(level, player, hand, itemStackInHand, result);
    }

    @Override
    public ItemStack getCraftingRemainingItem(ItemStack itemStack) {
        return DeferredRegisterItems.TOOL_DEVTOOL.get().getDefaultInstance();
    }

    @Override
    public boolean hasCraftingRemainingItem(ItemStack stack) {
        return true;
    }
}
