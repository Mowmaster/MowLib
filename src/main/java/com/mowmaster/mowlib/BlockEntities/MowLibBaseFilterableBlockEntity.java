package com.mowmaster.mowlib.BlockEntities;

import com.mowmaster.mowlib.MowLibUtils.MowLibBlockPosUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import com.mowmaster.mowlib.api.DefineLocations.IWorkCard;
import com.mowmaster.mowlib.api.TransportAndStorage.IFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class MowLibBaseFilterableBlockEntity extends MowLibBaseBlockEntity
{
    private ItemStackHandler baseInsertablesHandler = createPrivateInsertableItemHandler();
    private List<ItemStack> baseInsertablesStacksList = new ArrayList<>();

    public MowLibBaseFilterableBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    private static class PrivateInventorySlot {
        static final int FILTER = 0;
        static final int WORK_CARD = 1;
        static final int REDSTONE = 2;
        static final int LIGHT = 3;
    }

    //Filter
    //WorkCard
    //Redstone
    //Light
    private static int insertableHandlerSize = 4;
    private ItemStackHandler createPrivateInsertableItemHandler() {

        return new ItemStackHandler(insertableHandlerSize) {

            @Override
            protected void onLoad() {
                if(getSlots()<insertableHandlerSize)
                {
                    for(int i = 0; i < getSlots(); ++i) {
                        baseInsertablesStacksList.add(i,getStackInSlot(i));
                    }
                    setSize(insertableHandlerSize);
                    for(int j = 0;j<baseInsertablesStacksList.size();j++) {
                        setStackInSlot(j, baseInsertablesStacksList.get(j));
                    }
                }

                super.onLoad();
            }

            @Override
            protected void onContentsChanged(int slot) {
                if(!(baseInsertablesStacksList.size()>0))
                {
                    update();
                }
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                return switch (slot) {
                    case PrivateInventorySlot.FILTER -> stack.getItem() instanceof IFilterItem && !(stack.getItem().equals(DeferredRegisterItems.FILTER_BASE.get())) && !hasFilter();
                    case PrivateInventorySlot.WORK_CARD -> stack.getItem() instanceof IWorkCard && !hasWorkCard();
                    case PrivateInventorySlot.REDSTONE -> stack.is(Items.REDSTONE) && getRedstonePowerNeeded() < 15;
                    case PrivateInventorySlot.LIGHT -> stack.is(Items.GLOWSTONE) && !hasLight();
                    default -> false;
                };
            }
        };
    }

    public void dropInventoryItemsPrivate(Level worldIn, BlockPos pos) {
        MowLibItemUtils.dropInventoryItems(worldIn,pos,baseInsertablesHandler);
    }

    public List<ItemStack> dropInventoryItemsPrivateList() {
        List<ItemStack> returner = new ArrayList<>();
        for(int i = 0; i < baseInsertablesHandler.getSlots(); ++i) {
            returner.add(baseInsertablesHandler.getStackInSlot(i));
        }

        return returner;
    }
    
    /*============================================================================
    ==============================================================================
    ===========================      LIGHT START     =============================
    ==============================================================================
    ============================================================================*/

    public boolean attemptAddLight(ItemStack stack, @Nullable BlockState newState)
    {
        if (baseInsertablesHandler.isItemValid(PrivateInventorySlot.LIGHT, stack)) {
            baseInsertablesHandler.insertItem(PrivateInventorySlot.LIGHT, stack.split(1), false);
            if(newState !=null)level.setBlock(getPos(),newState,3);
            update();
            return true;
        } else {
            return false;
        }
    }

    public ItemStack removeLight(@Nullable BlockState newState)
    {
        if(hasLight())
        {
            ItemStack retItemStack = baseInsertablesHandler.extractItem(PrivateInventorySlot.LIGHT, 1, false);
            if(newState !=null)level.setBlock(getPos(),newState,3);
            update();
            return retItemStack;
        }
        else return ItemStack.EMPTY;
    }

    /*public ItemStack removeLight()
    {
        if(hasLight())
        {
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, state.getValue(FILTER_STATUS));
            if(getLightBrightness()<=1)
            {
                boolLight = true;
                ItemStack retItemStack = baseInsertablesHandler.extractItem(PrivateInventorySlot.LIGHT, 1, false);
                level.setBlock(getPos(),newstate,3);
                return retItemStack;
            }
            else
            {
                ItemStack retItemStack = baseInsertablesHandler.extractItem(PrivateInventorySlot.LIGHT, 1, false);
                state.updateNeighbourShapes(this.level,getPos(),1,3);
                return retItemStack;
            }

        }
        else return ItemStack.EMPTY;
    }*/

    /*public ItemStack removeAllLight()
    {
        if(hasLight())
        {
            BlockState state = level.getBlockState(getPos());
            BlockState newstate = MowLibColorReference.addColorToBlockState(DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get().defaultBlockState(),MowLibColorReference.getColorFromStateInt(state)).setValue(WATERLOGGED, state.getValue(WATERLOGGED)).setValue(FACING, state.getValue(FACING)).setValue(LIT, Boolean.valueOf(false)).setValue(FILTER_STATUS, state.getValue(FILTER_STATUS));
            ItemStack retItemStack = baseInsertablesHandler.extractItem(PrivateInventorySlot.LIGHT, baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.LIGHT).getCount(), false);
            level.setBlock(getPos(),newstate,3);
            return retItemStack;
        }
        else return ItemStack.EMPTY;
    }*/

    /*public int getLightBrightness()
    {
        return baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.LIGHT).getCount();
    }*/

    public boolean hasLight()
    {
        return !baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.LIGHT).isEmpty();
    }

    public void actionOnLightRemovedFromBlockEntity(int type) {
        // 0 = Dropped
        // 1 = Removed
    }

    /*============================================================================
    ==============================================================================
    ===========================       LIGHT END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================     FILTER START     =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasFilter()
    {
        return !baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.FILTER).isEmpty();
    }

    public ItemStack getFilterInBlockEntity()
    {
        return baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.FILTER);
    }

    public IFilterItem getIFilterItem()
    {
        if(hasFilter())
        {
            return (IFilterItem) getFilterInBlockEntity().getItem();
        }

        return null;
    }

    public ItemStack removeFilter(@Nullable BlockState newState) {
        if(newState !=null)
        {
            level.setBlock(getPos(),newState,3);
            update();
        }

        return baseInsertablesHandler.extractItem(PrivateInventorySlot.FILTER,1,false);
    }

    public boolean attemptAddFilter(ItemStack stack, @Nullable BlockState newState)
    {
        if (baseInsertablesHandler.isItemValid(PrivateInventorySlot.FILTER, stack)) {
            // stack.split might reduce `stack` to an empty stack, so if we need to use any property of the item being
            // insert we need to make a reference to it it prior to insertion.
            ItemStack toInsert = stack.split(1);
            baseInsertablesHandler.insertItem(PrivateInventorySlot.FILTER, toInsert, false);
            if(newState !=null)level.setBlock(getPos(),newState,3);
            update();
            return true;
        } else {
            return false;
        }
    }

    public void actionOnFilterRemovedFromBlockEntity(int type) {
        // 0 = Dropped
        // 1 = Removed
    }

    /*============================================================================
    ==============================================================================
    ===========================      FILTER END      =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    WORKCARD START    =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasWorkCard()
    {
        return !baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.WORK_CARD).isEmpty();
    }

    @Override
    public ItemStack getWorkCard() {
        return getWorkCardInPedestal();
    }

    public ItemStack getWorkCardInPedestal()
    {
        return baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.WORK_CARD);
    }

    public IWorkCard getIWorkCard()
    {
        if(hasWorkCard())
        {
            return (IWorkCard)getWorkCardInPedestal().getItem();
        }

        return null;
    }

    public ItemStack removeWorkCard(@Nullable BlockState newState) {
        if(newState !=null)
        {
            level.setBlock(getPos(),newState,3);
            update();
        }
        return baseInsertablesHandler.extractItem(PrivateInventorySlot.WORK_CARD,1,false);
    }

    public boolean attemptAddWorkCard(ItemStack stack, @Nullable BlockState newState)
    {
        if (baseInsertablesHandler.isItemValid(PrivateInventorySlot.WORK_CARD, stack)) {
            baseInsertablesHandler.insertItem(PrivateInventorySlot.WORK_CARD, stack.split(1), false);
            if(newState !=null) level.setBlock(getPos(),newState,3);
            update();
            return true;
        } else {
            return false;
        }
    }

    public void actionOnWorkCardRemovedFromBlockEntity(int type) {
        // 0 = Dropped
        // 1 = Removed
    }

    /*============================================================================
    ==============================================================================
    ===========================    WORKCARD  END     =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    REDSTONE START    =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasRedstone()
    {
        return !baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.REDSTONE).isEmpty();
    }

    public boolean attemptAddRedstone(ItemStack stack, @Nullable BlockState newState)
    {
        if(!hasRedstone() || getRedstonePowerNeeded()<15)
        {
            if(newState !=null)level.setBlock(getPos(),newState,3);
            baseInsertablesHandler.insertItem(PrivateInventorySlot.REDSTONE,stack.split(1),false);
            return true;
        }
        else return false;
    }

    public ItemStack removeRedstone(@Nullable BlockState newState)
    {
        if(newState !=null)
        {
            level.setBlock(getPos(),newState,3);
            update();
        }
        return baseInsertablesHandler.extractItem(PrivateInventorySlot.REDSTONE,1,false);
    }

    public ItemStack removeAllRedstone(@Nullable BlockState newState)
    {
        if(newState !=null)
        {
            level.setBlock(getPos(),newState,3);
            update();
        }
        return baseInsertablesHandler.extractItem(PrivateInventorySlot.REDSTONE,getRedstonePowerNeeded(),false);
    }

    public int getRedstonePowerNeeded()
    {
        return baseInsertablesHandler.getStackInSlot(PrivateInventorySlot.REDSTONE).getCount();
    }

    public boolean isPedestalBlockPowered(MowLibBaseFilterableBlockEntity baseFilterableBlockEntity)
    {
        if(baseFilterableBlockEntity.hasRedstone())
        {
            //hasRedstone should mean if theres a signal, its off (reverse of normal)
            return (this.getLevel().hasNeighborSignal(baseFilterableBlockEntity.getBlockPos()))?((baseFilterableBlockEntity.getRedstonePower()>=baseFilterableBlockEntity.getRedstonePowerNeeded())?(false):(true)):(true);
        }

        return baseFilterableBlockEntity.getRedstonePower() > 0;
    }

    public int getRedstonePower() {
        /*System.out.println(this.getLevel().getSignal(this.getBlockPos(), Direction.NORTH));
        System.out.println(this.getLevel().getDirectSignal(this.getBlockPos(), Direction.NORTH));

        //Redstone Dust Linked to it ONLY
        System.out.println(this.getLevel().getDirectSignalTo(this.getBlockPos()));

        //Any Redstone Signal (Lever, torch, dust, whatever
        System.out.println(this.getLevel().getBestNeighborSignal(this.getBlockPos()));*/

        return this.getLevel().getBestNeighborSignal(this.getBlockPos());
    }

    public void actionOnRedstoneRemovedFromBlockEntity(int type) {
        // 0 = Dropped
        // 1 = Removed
    }

    /*============================================================================
    ==============================================================================
    ===========================    REDSTONE END      =============================
    ==============================================================================
    ============================================================================*/

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        CompoundTag invPrivateTag = p_155245_.getCompound("inv_mowlibfilterable_private");
        baseInsertablesHandler.deserializeNBT(invPrivateTag);
    }

    @Override
    public CompoundTag save(CompoundTag p_58888_) {
        p_58888_.put("inv_mowlibfilterable_private", baseInsertablesHandler.serializeNBT());
        return super.save(p_58888_);
    }
}
