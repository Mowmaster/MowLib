package com.mowmaster.mowlib.BlockEntities;

import com.google.common.collect.Maps;
import com.mowmaster.mowlib.Blocks.BaseBlocks.BaseColoredBlock;
import com.mowmaster.mowlib.Items.BaseRepairNote;
import com.mowmaster.mowlib.Items.ColorApplicator;
import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import com.mowmaster.mowlib.MowLibUtils.ContainerUtils;
import com.mowmaster.mowlib.MowLibUtils.MessageUtils;
import com.mowmaster.mowlib.Recipes.MachineBlockRepairItemsHintRecipe;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.stream.IntStream;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;


public class BaseBuiltMachineBlock extends BaseColoredBlock implements EntityBlock
{
    public BaseBuiltMachineBlock(BlockBehaviour.Properties p_152915_) {
        super(p_152915_);
        this.registerDefaultState(ColorReference.addColorToBlockState(this.defaultBlockState(),ColorReference.DEFAULTCOLOR));
    }

    @Nullable
    public MachineBlockRepairItemsHintRecipe getRecipe(Level level, ItemStack stackIn) {
        Container cont = ContainerUtils.getContainer(1);
        cont.setItem(-1,stackIn);
        List<MachineBlockRepairItemsHintRecipe> recipes = level.getRecipeManager().getRecipesFor(MachineBlockRepairItemsHintRecipe.Type.INSTANCE,cont,level);
        return recipes.size() > 0 ? level.getRecipeManager().getRecipesFor(MachineBlockRepairItemsHintRecipe.Type.INSTANCE,cont,level).get(0) : null;
    }

    public String getProcessResultTitle(MachineBlockRepairItemsHintRecipe recipe) {
        return (recipe == null)?(""):(recipe.getResultTitle());
    }

    public String getProcessResultDescription(MachineBlockRepairItemsHintRecipe recipe) {
        return (recipe == null)?(""):(recipe.getResultDescription());
    }

    public boolean getProcessResultIsLocalized(MachineBlockRepairItemsHintRecipe recipe) {
        return (recipe == null)?(false):(recipe.getResultLocalized());
    }

    @Override
    public InteractionResult use(BlockState p_60503_, Level p_60504_, BlockPos p_60505_, Player p_60506_, InteractionHand p_60507_, BlockHitResult p_60508_) {

        if(!p_60504_.isClientSide())
        {
            ItemStack itemInHand = p_60506_.getItemInHand(p_60507_);
            ItemStack itemInMainHand = p_60506_.getMainHandItem();
            ItemStack itemInOffHand = p_60506_.getOffhandItem();

            if(itemInMainHand.getItem() instanceof ColorApplicator)
            {
                int getColor = ColorReference.getColorFromItemStackInt(itemInMainHand);
                BlockState newState = ColorReference.addColorToBlockState(p_60503_,getColor);
                p_60504_.setBlock(p_60505_,newState,3);
                return InteractionResult.SUCCESS;
            }
            else if(itemInOffHand.getItem() instanceof ColorApplicator)
            {
                int getColor = ColorReference.getColorFromItemStackInt(itemInOffHand);
                BlockState newState = ColorReference.addColorToBlockState(p_60503_,getColor);
                p_60504_.setBlock(p_60505_,newState,3);
                return InteractionResult.SUCCESS;
            }
            else if(p_60504_.getBlockEntity(p_60505_) instanceof BaseBuiltMachineBlockEntity table)
            {
                if(!table.isFullyRepaired())
                {
                    if(itemInMainHand.getItem() instanceof BaseRepairNote scroll)
                    {
                        scroll.setMachineBlock(itemInMainHand,this);
                        scroll.setRepairItem(itemInMainHand,table.getNextRepairItem());
                        if(!scroll.getRepairItem(itemInMainHand).isEmpty())
                        {
                            ItemStack repairItemOnScroll = scroll.getRepairItem(itemInMainHand);
                            MachineBlockRepairItemsHintRecipe recipe = getRecipe(p_60504_,repairItemOnScroll);
                            String title = getProcessResultTitle(recipe);
                            String description = getProcessResultDescription(recipe);
                            boolean isLocalized = getProcessResultIsLocalized(recipe);
                            scroll.setHintTitle(itemInMainHand,title);
                            scroll.setHintDescription(itemInMainHand,description);
                            scroll.setHintLocalization(itemInMainHand,isLocalized);
                            if(scroll.getItemFound(itemInMainHand))scroll.setItemFound(itemInMainHand,false);
                            MessageUtils.messagePopup(p_60506_, ChatFormatting.GOLD,MODID + ".hint.scroll_made");
                        }
                        return InteractionResult.SUCCESS;
                    }
                    else if(table.isValidItem(itemInMainHand))
                    {
                        if(table.addRepairItem(p_60506_,itemInMainHand,false))
                        {
                            itemInMainHand.shrink(1);
                            Inventory inv = p_60506_.getInventory();
                            ItemStack itemFound = ItemStack.EMPTY;
                            itemFound = IntStream.range(0,inv.getContainerSize())
                                    .mapToObj((inv)::getItem)//Function being applied to each interval
                                    .filter(itemStack -> itemStack.getItem() instanceof BaseRepairNote)
                                    //.filter(itemStack -> ((T2RepairScroll)itemStack.getItem()).getRepairItem(itemStack).getItem().equals(itemInMainHand.getItem()))
                                    .findFirst().orElse(ItemStack.EMPTY);
                            if(!itemFound.isEmpty())
                            {
                                for(int i=0;i<inv.getContainerSize();i++)
                                {
                                    if(inv.getItem(i).getItem().equals(itemFound.getItem()))
                                    {
                                        ItemStack stackie = inv.getItem(i);
                                        BaseRepairNote scroll = ((BaseRepairNote)stackie.getItem());
                                        scroll.setMachineBlock(stackie, Blocks.AIR);
                                        scroll.setRepairItem(stackie,ItemStack.EMPTY);
                                        scroll.setHintTitle(stackie,"");
                                        scroll.setHintDescription(stackie, "");
                                        scroll.setHintLocalization(stackie, false);
                                        Map<Enchantment, Integer> enchantsNone = Maps.<Enchantment, Integer>newLinkedHashMap();
                                        EnchantmentHelper.setEnchantments(enchantsNone,stackie);
                                        inv.setItem(i,stackie);
                                        break;
                                    }
                                }
                            }

                            if(table.isFullyRepaired()) { MessageUtils.messagePopup(p_60506_, ChatFormatting.GREEN,MODID + ".hint.repair_finished"); }
                            else { MessageUtils.messagePopup(p_60506_, ChatFormatting.GOLD,MODID + ".hint.repair_made"); }

                            return InteractionResult.SUCCESS;
                        }
                    }
                }
            }
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public void onRemove(BlockState p_60515_, Level p_60516_, BlockPos p_60517_, BlockState p_60518_, boolean p_60519_) {
        if(p_60515_.getBlock() != p_60518_.getBlock())
        {
            BlockEntity blockEntity = p_60516_.getBlockEntity(p_60517_);
            if(blockEntity instanceof BaseBuiltMachineBlockEntity t1blockentity) {
                t1blockentity.dropInventoryItems(p_60516_,p_60517_);
            }
            super.onRemove(p_60515_, p_60516_, p_60517_, p_60518_, p_60519_);
        }
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos p_153215_, BlockState p_153216_) {
        return null;
    }
}
