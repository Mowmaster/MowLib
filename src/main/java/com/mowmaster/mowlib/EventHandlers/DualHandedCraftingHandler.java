package com.mowmaster.mowlib.EventHandlers;

import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import com.mowmaster.mowlib.MowLibUtils.ContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Recipes.InWorldDualHandedCrafting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

@Mod.EventBusSubscriber
public class DualHandedCraftingHandler
{

    @SubscribeEvent()
    public static void dualHandedCrafting(PlayerInteractEvent.RightClickBlock event) {

        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getPlayer();

        if(!level.isClientSide())
        {
            if(player.getMainHandItem() != null && player.getOffhandItem() != null && event.getHand().equals(InteractionHand.MAIN_HAND))
            {
                ItemStack blockTarget = new ItemStack(state.getBlock().asItem());
                if(blockTarget != null)
                {
                    InWorldDualHandedCrafting getRecipe = getRecipe(level,blockTarget,player.getMainHandItem(),player.getOffhandItem());
                    if(getRecipe != null)
                    {
                        ItemStack getResultItem = getBlockItemResult(getRecipe).stream().findFirst().get();
                        if(getResultItem != null)
                        {
                            if(!player.isCreative())
                            {
                                if(consumeMainHandItemOrDurability(getRecipe))
                                {
                                    if(player.getMainHandItem().isDamageableItem()) {
                                        MowLibItemUtils.damageItem(player.getMainHandItem(),1);
                                    }
                                    else {
                                        player.getMainHandItem().shrink(1);
                                    }
                                }

                                if(consumeOffHandItemOrDurability(getRecipe))
                                {
                                    if(player.getOffhandItem().isDamageableItem()) {
                                        MowLibItemUtils.damageItem(player.getOffhandItem(),1);
                                    }
                                    else {
                                        player.getOffhandItem().shrink(1);
                                    }
                                }
                            }

                            if(Block.byItem(getResultItem.getItem()) != null && Block.byItem(getResultItem.getItem()) != Blocks.AIR)
                            {
                                BlockState blockToSet = Block.byItem(getResultItem.getItem()).defaultBlockState();
                                if(blockToSet.hasProperty(ColorReference.COLOR_RED) && blockToSet.hasProperty(ColorReference.COLOR_GREEN) && blockToSet.hasProperty(ColorReference.COLOR_BLUE))
                                {
                                    if(player.getMainHandItem().getItem() instanceof ColorApplicator)blockToSet = ColorReference.addColorToBlockState(blockToSet,ColorReference.getColorFromItemStackInt(player.getMainHandItem()));
                                    else if(player.getOffhandItem().getItem() instanceof ColorApplicator)blockToSet = ColorReference.addColorToBlockState(blockToSet,ColorReference.getColorFromItemStackInt(player.getOffhandItem()));
                                //Include options for dyes sometime too???
                                }

                                if(state.hasProperty(BlockStateProperties.WATERLOGGED) && blockToSet.hasProperty(BlockStateProperties.WATERLOGGED))blockToSet = blockToSet.setValue(BlockStateProperties.WATERLOGGED,state.getValue(BlockStateProperties.WATERLOGGED));
                                if(state.hasProperty(BlockStateProperties.SLAB_TYPE) && blockToSet.hasProperty(BlockStateProperties.SLAB_TYPE))blockToSet = blockToSet.setValue(BlockStateProperties.SLAB_TYPE,state.getValue(BlockStateProperties.SLAB_TYPE));
                                if(state.hasProperty(HorizontalDirectionalBlock.FACING) && blockToSet.hasProperty(HorizontalDirectionalBlock.FACING))blockToSet = blockToSet.setValue(HorizontalDirectionalBlock.FACING,state.getValue(HorizontalDirectionalBlock.FACING));
                                if(state.hasProperty(BlockStateProperties.HALF) && blockToSet.hasProperty(BlockStateProperties.HALF))blockToSet = blockToSet.setValue(BlockStateProperties.HALF,state.getValue(BlockStateProperties.HALF));
                                if(state.hasProperty(BlockStateProperties.STAIRS_SHAPE) && blockToSet.hasProperty(BlockStateProperties.STAIRS_SHAPE))blockToSet = blockToSet.setValue(BlockStateProperties.STAIRS_SHAPE,state.getValue(BlockStateProperties.STAIRS_SHAPE));
                                if(state.hasProperty(BlockStateProperties.VERTICAL_DIRECTION) && blockToSet.hasProperty(BlockStateProperties.VERTICAL_DIRECTION))blockToSet = blockToSet.setValue(BlockStateProperties.VERTICAL_DIRECTION,state.getValue(BlockStateProperties.VERTICAL_DIRECTION));
                                if(state.hasProperty(BlockStateProperties.DOOR_HINGE) && blockToSet.hasProperty(BlockStateProperties.DOOR_HINGE))blockToSet = blockToSet.setValue(BlockStateProperties.DOOR_HINGE,state.getValue(BlockStateProperties.DOOR_HINGE));
                                if(state.hasProperty(BlockStateProperties.ROTATION_16) && blockToSet.hasProperty(BlockStateProperties.ROTATION_16))blockToSet = blockToSet.setValue(BlockStateProperties.ROTATION_16,state.getValue(BlockStateProperties.ROTATION_16));
                                if(state.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF) && blockToSet.hasProperty(BlockStateProperties.DOUBLE_BLOCK_HALF))blockToSet = blockToSet.setValue(BlockStateProperties.DOUBLE_BLOCK_HALF,state.getValue(BlockStateProperties.DOUBLE_BLOCK_HALF));
                                if(state.hasProperty(BlockStateProperties.ORIENTATION) && blockToSet.hasProperty(BlockStateProperties.ORIENTATION))blockToSet = blockToSet.setValue(BlockStateProperties.ORIENTATION,state.getValue(BlockStateProperties.ORIENTATION));
                                if(state.hasProperty(BlockStateProperties.HORIZONTAL_FACING) && blockToSet.hasProperty(BlockStateProperties.HORIZONTAL_FACING))blockToSet = blockToSet.setValue(BlockStateProperties.HORIZONTAL_FACING,state.getValue(BlockStateProperties.HORIZONTAL_FACING));
                                if(state.hasProperty(BlockStateProperties.FACING) && blockToSet.hasProperty(BlockStateProperties.FACING))blockToSet = blockToSet.setValue(BlockStateProperties.FACING,state.getValue(BlockStateProperties.FACING));
                                if(state.hasProperty(BlockStateProperties.AXIS) && blockToSet.hasProperty(BlockStateProperties.AXIS))blockToSet = blockToSet.setValue(BlockStateProperties.AXIS,state.getValue(BlockStateProperties.AXIS));
                                if(state.hasProperty(BlockStateProperties.HORIZONTAL_AXIS) && blockToSet.hasProperty(BlockStateProperties.HORIZONTAL_AXIS))blockToSet = blockToSet.setValue(BlockStateProperties.HORIZONTAL_AXIS,state.getValue(BlockStateProperties.HORIZONTAL_AXIS));
                                if(state.hasProperty(BlockStateProperties.LIT) && blockToSet.hasProperty(BlockStateProperties.LIT))blockToSet = blockToSet.setValue(BlockStateProperties.LIT,state.getValue(BlockStateProperties.LIT));
                                if(state.hasProperty(BlockStateProperties.INVERTED) && blockToSet.hasProperty(BlockStateProperties.INVERTED))blockToSet = blockToSet.setValue(BlockStateProperties.INVERTED,state.getValue(BlockStateProperties.INVERTED));
                                if(state.hasProperty(BlockStateProperties.HAS_BOOK) && blockToSet.hasProperty(BlockStateProperties.HAS_BOOK))blockToSet = blockToSet.setValue(BlockStateProperties.HAS_BOOK,state.getValue(BlockStateProperties.HAS_BOOK));

                                level.setBlockAndUpdate(pos,blockToSet);
                                //level.setBlockAndUpdate(pos,Blocks.BEDROCK.defaultBlockState());
                            }
                            else
                            {
                                level.setBlockAndUpdate(pos,Blocks.AIR.defaultBlockState());
                                if(ColorReference.isColorItem(getResultItem))
                                {
                                    if(player.getMainHandItem().getItem() instanceof ColorApplicator)ColorReference.addColorToItemStack(getResultItem,ColorReference.getColorFromItemStackInt(player.getMainHandItem()));
                                    else if(player.getOffhandItem().getItem() instanceof ColorApplicator)ColorReference.addColorToItemStack(getResultItem,ColorReference.getColorFromItemStackInt(player.getOffhandItem()));
                                }
                                MowLibItemUtils.spawnItemStack(level, pos.getX(), pos.getY(), pos.getZ(), getResultItem);
                            }
                        }
                    }
                }
            }
        }

    }



    @Nullable
    protected static InWorldDualHandedCrafting getRecipe(Level level, ItemStack targetBlockItem, ItemStack mainHandItem, ItemStack offHandItem) {
        Container cont = ContainerUtils.getContainer(3);
        cont.setItem(-1,targetBlockItem);
        cont.setItem(-1,mainHandItem);
        cont.setItem(-1,offHandItem);
        List<InWorldDualHandedCrafting> recipes = level.getRecipeManager().getRecipesFor(InWorldDualHandedCrafting.Type.INSTANCE,cont,level);
        return recipes.size() > 0 ? level.getRecipeManager().getRecipesFor(InWorldDualHandedCrafting.Type.INSTANCE,cont,level).get(0) : null;
    }

    protected static Collection<ItemStack> getBlockItemResult(InWorldDualHandedCrafting recipe) {
        return (recipe == null)?(Arrays.asList(ItemStack.EMPTY)):(Collections.singleton(recipe.getResultItem()));
    }

    protected static Boolean consumeMainHandItemOrDurability(InWorldDualHandedCrafting recipe) {
        return (recipe == null)?(true):(recipe.consumeMainHand());
    }

    protected static Boolean consumeOffHandItemOrDurability(InWorldDualHandedCrafting recipe) {
        return (recipe == null)?(true):(recipe.consumeOffHand());
    }

    /*
@Mod.EventBusSubscriber
public class DualHandedCraftingHandler
{
    @SubscribeEvent()
    public static void hammerCrafting(PlayerInteractEvent.RightClickBlock event) {
        Level level = event.getWorld();
        BlockPos pos = event.getPos();
        BlockState state = level.getBlockState(pos);
        Player player = event.getPlayer();

        if(!level.isClientSide)
        {
            if(player.getMainHandItem() != null)
            {
                if(player.getMainHandItem().getItem() instanceof BaseHammerItem)
                {
                    ItemStack blockTarget = new ItemStack(state.getBlock().asItem());
                    if(blockTarget != null)
                    {
                        TagKey<Item> LOGS = ItemTags.LOGS;
                        if(blockTarget.is(LOGS) && player.getOffhandItem().getItem() instanceof AxeItem)
                        {
                            level.setBlock(pos, DeferredRegisterBlocks.BASIN_WOOD.get().defaultBlockState(),2);
                        }
                    }
                }
            }
        }
    }
}
     */
}
