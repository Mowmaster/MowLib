package com.mowmaster.mowlib.BlockEntities;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.Items.WorkCards.WorkCardBE;
import com.mowmaster.mowlib.MowLibUtils.*;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import com.mowmaster.mowlib.api.DefineLocations.IWorkCard;
import com.mowmaster.mowlib.api.Tools.IMowLibTool;
import com.mowmaster.mowlib.api.TransportAndStorage.IFilterItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Map;

public class MowLibBaseFilterableBlock extends MowLibBaseBlock
{
    //0 = default
    //1= whitelist
    //2= blacklist
    public static final IntegerProperty FILTER_STATUS = IntegerProperty.create("filter_status", 0, 2);
    public static final BooleanProperty WORKCARD_STATUS = BooleanProperty.create("workcard_status");
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public static final IntegerProperty REDSTONE_STATUS = IntegerProperty.create("redstone_status", 0, 15);

    public MowLibBaseFilterableBlock(Properties p_152915_) {
        super(p_152915_);
    }

    @Override
    public void attack(BlockState p_60499_, Level p_60500_, BlockPos p_60501_, Player p_60502_) {
        if(!p_60500_.isClientSide())
        {
            if(p_60502_ instanceof FakePlayer){ super.attack(p_60499_, p_60500_, p_60501_, p_60502_); }

            BlockEntity blockEntity = p_60500_.getBlockEntity(p_60501_);
            if(blockEntity instanceof MowLibBaseFilterableBlockEntity baseFilterableBlockEntity)
            {
                ItemStack itemInHand = p_60502_.getMainHandItem();
                ItemStack itemInOffHand = p_60502_.getOffhandItem();

                if(baseFilterableBlockEntity.hasFilter() && itemInOffHand.is(DeferredRegisterItems.TOOL_FILTERTOOL.get()))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,baseFilterableBlockEntity.removeFilter(null));
                    baseFilterableBlockEntity.actionOnFilterRemovedFromBlockEntity(1);
                }
                else if(baseFilterableBlockEntity.hasLight() && itemInOffHand.is(Items.GLOWSTONE))
                {
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,baseFilterableBlockEntity.removeLight(null));
                    baseFilterableBlockEntity.actionOnLightRemovedFromBlockEntity(1);
                }
                else if(baseFilterableBlockEntity.hasRedstone() && itemInOffHand.is(Items.REDSTONE))
                {
                    if(p_60502_.isShiftKeyDown())
                    {
                        ItemHandlerHelper.giveItemToPlayer(p_60502_,baseFilterableBlockEntity.removeAllRedstone(null));
                    }
                    else
                    {
                        ItemHandlerHelper.giveItemToPlayer(p_60502_,baseFilterableBlockEntity.removeRedstone(null));
                    }
                    
                    baseFilterableBlockEntity.actionOnRedstoneRemovedFromBlockEntity(1);
                }
                else if(baseFilterableBlockEntity.hasWorkCard() && itemInOffHand.is(DeferredRegisterItems.TOOL_WORKTOOL.get()))
                {
                    baseFilterableBlockEntity.actionOnWorkCardRemovedFromBlockEntity(1);
                    ItemHandlerHelper.giveItemToPlayer(p_60502_,baseFilterableBlockEntity.removeWorkCard(null));
                }
            }
        }
        super.attack(p_60499_, p_60500_, p_60501_, p_60502_);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {
        if(p_60504_.isClientSide())
        {
            ItemStack itemInHand = p_60506_.getMainHandItem();
            if(itemInHand.getItem() instanceof WorkCardBE)
            {
                return InteractionResult.FAIL;
            }
        }
        else
        {
            BlockEntity blockEntity = p_60504_.getBlockEntity(p_60505_);
            if(blockEntity instanceof MowLibBaseFilterableBlockEntity baseFilterableBlockEntity)
            {
                ItemStack itemInHand = p_60506_.getMainHandItem();
                ItemStack itemInOffHand = p_60506_.getOffhandItem();

                int getColor;
                int currentColor;
                Component sameColor;
                BlockState newState;
                List<Item> DYES = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes"))).stream().toList();

                if(itemInHand.getItem() instanceof IMowLibTool)
                {
                    return InteractionResult.FAIL;
                }
                else if (p_60506_.getItemInHand(p_60507_).getItem() instanceof ColorApplicator) {


                    getColor = MowLibColorReference.getColorFromItemStackInt(p_60506_.getItemInHand(p_60507_));
                    currentColor = MowLibColorReference.getColorFromStateInt(p_60503_);
                    if (currentColor != getColor) {
                        newState = MowLibColorReference.addColorToBlockState(p_60503_, getColor);
                        p_60504_.setBlock(p_60505_, newState, 3);
                        return InteractionResult.SUCCESS;
                    }
                    else {
                        MowLibMessageUtils.messagePlayerChat(p_60506_, ChatFormatting.RED,"mowlib.recolor.message_sameColor");
                        return InteractionResult.FAIL;
                    }
                }
                else if(itemInOffHand.getItem() instanceof IFilterItem)
                {
                    if(baseFilterableBlockEntity.attemptAddFilter(itemInOffHand,null)) {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem() instanceof IWorkCard)
                {
                    if(baseFilterableBlockEntity.attemptAddWorkCard(itemInOffHand, null))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(Items.GLOWSTONE))
                {
                    if(baseFilterableBlockEntity.attemptAddLight(itemInOffHand, null))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(itemInOffHand.getItem().equals(Items.REDSTONE))
                {
                    if(baseFilterableBlockEntity.attemptAddRedstone(itemInOffHand, null))
                    {
                        return InteractionResult.SUCCESS;
                    }
                }
                else if(DYES.contains(itemInOffHand.getItem()))
                {
                    getColor = MowLibColorReference.getColorFromDyeInt(itemInOffHand);
                    currentColor = MowLibColorReference.getColorFromStateInt(p_60503_);
                    if (currentColor != getColor) {
                        newState = MowLibColorReference.addColorToBlockState(p_60503_, getColor);
                        p_60504_.setBlock(p_60505_, newState, 3);
                        return InteractionResult.SUCCESS;
                    } else {
                        MowLibMessageUtils.messagePlayerChat(p_60506_, ChatFormatting.RED,"mowlib.recolor.message_sameColor");
                        return InteractionResult.FAIL;
                    }

                }
                else
                {
                    if(!itemInHand.isEmpty())
                    {
                        if(itemInHand.getItem() instanceof WorkCardBE)
                        {
                            return InteractionResult.FAIL;
                        }
                    }
                    else return InteractionResult.FAIL;
                }

            }
        }

        return InteractionResult.SUCCESS;
    }

    @Override
    public int getLightEmission(BlockState state, BlockGetter level, BlockPos pos) {
        return (state.getValue(LIT))?(15):(0);
    }

    @Override
    public boolean shouldCheckWeakPower(BlockState state, SignalGetter level, BlockPos pos, Direction side) {
        return true;
    }

    @Override
    public boolean hasAnalogOutputSignal(BlockState p_60457_) {
        //super.hasAnalogOutputSignal(p_60457_);
        return true;
    }

    @Override
    public int getAnalogOutputSignal(BlockState p_60487_, Level p_60488_, BlockPos p_60489_) {
        return super.getAnalogOutputSignal(p_60487_, p_60488_, p_60489_);
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter world, BlockPos pos, @javax.annotation.Nullable Direction direction) {
        return true;
    }

    @Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
        if(p_60515_.getBlock() != p_60518_.getBlock())
        {
            BlockEntity blockEntity = p_60516_.getBlockEntity(p_60517_);
            if(blockEntity instanceof MowLibBaseFilterableBlockEntity baseFilterableBlockEntity) {
                baseFilterableBlockEntity.dropInventoryItemsPrivate(p_60516_,p_60517_);
                p_60516_.updateNeighbourForOutputSignal(p_60517_,p_60518_.getBlock());
            }
            p_60516_.removeBlock(p_60517_,false);
            super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
        }
    }
}
