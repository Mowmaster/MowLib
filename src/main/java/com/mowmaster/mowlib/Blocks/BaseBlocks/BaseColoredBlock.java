package com.mowmaster.mowlib.Blocks.BaseBlocks;

import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import com.mowmaster.mowlib.api.IColorableBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ShulkerBoxBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.registries.ForgeRegistries;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MessageUtils.getMowLibComponentLocalized;

import net.minecraft.world.level.block.state.BlockBehaviour.Properties;

public class BaseColoredBlock extends Block implements IColorableBlock {

    public BaseColoredBlock(Properties p_152915_)
    {
        super(p_152915_);
        this.registerDefaultState(ColorReference.addColorToBlockState(this.defaultBlockState(),ColorReference.DEFAULTCOLOR));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56120_) {
        p_56120_.add(COLOR_RED).add(COLOR_GREEN).add(COLOR_BLUE);
    }

    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext p_56089_) {
        BlockState blockstate = p_56089_.getLevel().getBlockState(p_56089_.getClickedPos());
        if (blockstate.is(this))
        {
            int getColor = ColorReference.getColorFromStateInt(blockstate);
            return ColorReference.addColorToBlockState(this.defaultBlockState(),getColor);
        }
        else return super.getStateForPlacement(p_56089_);
    }

    @Override
    public ItemStack getCloneItemStack(BlockState state, HitResult target, BlockGetter world, BlockPos pos, Player player) {
        ItemStack picked = state.getBlock().getCloneItemStack(world, pos, state);
        int getColor = ColorReference.getColorFromStateInt(state);
        return ColorReference.addColorToItemStack(picked,getColor);
    }

    @Override
    public void setPlacedBy(Level p_49847_, BlockPos p_49848_, BlockState p_49849_, @Nullable LivingEntity p_49850_, ItemStack p_49851_) {
        if(p_49850_ instanceof Player)
        {
            Player player = ((Player)p_49850_);
            if(player.getOffhandItem().getItem() instanceof ColorApplicator)
            {
                int getColor = ColorReference.getColorFromItemStackInt(player.getOffhandItem());
                BlockState newState = ColorReference.addColorToBlockState(p_49849_,getColor);
                p_49847_.setBlock(p_49848_,newState,3);
            }
            else
            {
                int getColor = ColorReference.getColorFromItemStackInt(p_49851_);
                BlockState newState = ColorReference.addColorToBlockState(p_49849_,getColor);
                p_49847_.setBlock(p_49848_,newState,3);
            }
        }

        super.setPlacedBy(p_49847_, p_49848_, p_49849_, p_49850_, p_49851_);
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        int getColor;
        int currentColor;
        Component sameColor;
        BlockState newState;
        List<Item> DYES = ForgeRegistries.ITEMS.tags().getTag(ItemTags.create(new ResourceLocation("forge", "dyes"))).stream().toList();

        ItemStack itemInHand = p_60506_.getMainHandItem();
        ItemStack itemInOffHand = p_60506_.getOffhandItem();

        if(p_60506_.getItemInHand(p_60507_).getItem() instanceof ColorApplicator)
        {
            getColor = ColorReference.getColorFromItemStackInt(p_60506_.getItemInHand(p_60507_));
            currentColor = ColorReference.getColorFromStateInt(p_60503_);
            if(currentColor != getColor)
            {
                newState = ColorReference.addColorToBlockState(p_60503_,getColor);
                p_60504_.setBlock(p_60505_,newState,3);
                //p_60504_.markAndNotifyBlock(p_60505_,null,p_60503_,newState,3,1);
                return InteractionResult.SUCCESS;
            }
            sameColor = getMowLibComponentLocalized(".recolor.message_sameColor", ChatFormatting.RED);
            if(p_60504_.isClientSide)p_60506_.displayClientMessage(sameColor, true);
            return InteractionResult.FAIL;

        }
        else if(DYES.contains(itemInOffHand.getItem()))
        {
            getColor = ColorReference.getColorFromDyeInt(itemInOffHand);
            currentColor = ColorReference.getColorFromStateInt(p_60503_);
            if (currentColor != getColor) {
                newState = ColorReference.addColorToBlockState(p_60503_, getColor);
                p_60504_.setBlock(p_60505_, newState, 3);
                return InteractionResult.SUCCESS;
            } else {
                sameColor = getMowLibComponentLocalized(".recolor.message_sameColor", ChatFormatting.RED);
                if(p_60504_.isClientSide)p_60506_.displayClientMessage(sameColor, true);
                return InteractionResult.FAIL;
            }

        }

        return super.use(p_60503_, p_60504_, p_60505_, p_60506_, p_60507_, p_60508_);
    }

    //Should Fix Building Gadgets dropps issues
    //https://github.com/Direwolf20-MC/MiningGadgets/blob/1.19/src/main/java/com/direwolf20/mininggadgets/common/tiles/RenderBlockTileEntity.java#L444
    @Override
    public List<ItemStack> getDrops(BlockState p_60537_, LootContext.Builder p_60538_) {
        if (p_60537_.getBlock() instanceof BaseColoredBlock) {
            List<ItemStack> stacks = new ArrayList<>();
            ItemStack itemstack = new ItemStack(this);
            int getColor = ColorReference.getColorFromStateInt(p_60537_);
            ItemStack newStack = ColorReference.addColorToItemStack(itemstack,getColor);
            newStack.setCount(1);
            stacks.add(newStack);
            return stacks;
        }
        return super.getDrops(p_60537_, p_60538_);
    }

    /*@Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
        if(!p_60516_.isClientSide() && p_60519_)
        {
            if (p_60518_.getBlock() instanceof BaseColoredBlock) {
                if (!p_60516_.isClientSide) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = ColorReference.getColorFromStateInt(p_60518_);
                    ItemStack newStack = ColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(p_60516_, (double)p_60517_.getX() + 0.5D, (double)p_60517_.getY() + 0.5D, (double)p_60517_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_60516_.addFreshEntity(itementity);
                }
            }
        }
        super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
    }*/

    @Override
    public boolean canDropFromExplosion(BlockState state, BlockGetter level, BlockPos pos, Explosion explosion) {
        return true;
    }

    /*@Override
    public void onBlockExploded(BlockState state, Level level, BlockPos pos, Explosion explosion) {
        if(!level.isClientSide())
        {
            if (state.getBlock() instanceof BaseColoredBlock) {
                if (!level.isClientSide) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = ColorReference.getColorFromStateInt(state);
                    ItemStack newStack = ColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(level, (double)pos.getX() + 0.5D, (double)pos.getY() + 0.5D, (double)pos.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    level.addFreshEntity(itementity);
                }
            }
        }
        super.onBlockExploded(state, level, pos, explosion);
    }

    @Override
    public void playerWillDestroy(Level p_56212_, BlockPos p_56213_, BlockState p_56214_, Player p_56215_) {

        if(!p_56212_.isClientSide())
        {
            if (p_56214_.getBlock() instanceof BaseColoredBlock) {
                if (!p_56212_.isClientSide && !p_56215_.isCreative()) {
                    ItemStack itemstack = new ItemStack(this);
                    int getColor = ColorReference.getColorFromStateInt(p_56214_);
                    ItemStack newStack = ColorReference.addColorToItemStack(itemstack,getColor);
                    newStack.setCount(1);
                    ItemEntity itementity = new ItemEntity(p_56212_, (double)p_56213_.getX() + 0.5D, (double)p_56213_.getY() + 0.5D, (double)p_56213_.getZ() + 0.5D, newStack);
                    itementity.setDefaultPickUpDelay();
                    p_56212_.addFreshEntity(itementity);
                }
            }
        }
        super.playerWillDestroy(p_56212_, p_56213_, p_56214_, p_56215_);
    }*/

    @Override
    public void appendHoverText(ItemStack p_49816_, @org.jetbrains.annotations.Nullable BlockGetter p_49817_, List<Component> p_49818_, TooltipFlag p_49819_) {
        super.appendHoverText(p_49816_, p_49817_, p_49818_, p_49819_);
    }
}
