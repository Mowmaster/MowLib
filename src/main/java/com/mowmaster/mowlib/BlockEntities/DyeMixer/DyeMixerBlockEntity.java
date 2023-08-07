/*
package com.mowmaster.mowlib.BlockEntities.DyeMixer;

import com.mowmaster.mowlib.BlockEntities.BaseBuiltMachineBlockEntity;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Items.EffectItemBase;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.MowLibUtils.MowLibEffectUtils;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class DyeMixerBlockEntity extends BaseBuiltMachineBlockEntity {

    private ItemStack getDyeCrafted = ItemStack.EMPTY;
    private int getRedDyeValue = 0;
    private int getGreenDyeValue = 0;
    private int getBlueDyeValue = 0;

    public DyeMixerBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    */
/*============================================================================
    ==============================================================================
    =====================   BASE CLASS OVERRIDES - START   =======================
    ==============================================================================
    ============================================================================*//*


    @Override
    public void update()
    {
        getScrollCraftingOutput();
        BlockState state = level.getBlockState(getPos());
        this.level.sendBlockUpdated(getPos(), state, state, 3);
        this.setChanged();
    }

    @Override
    public int getRepairSlotsForRepairs()
    {
        //1 slot for bucket
        //1 slot for crafting table
        //1 slot for button
        return 3;
    }

    @Override
    public Block getBlockForThisBlockEntity()
    {
        return DeferredRegisterTileBlocks.BLOCK_CRAFTER_SCROLL_T15.get();
    }

    */
/*============================================================================
    ==============================================================================
    =====================    BASE CLASS OVERRIDES - END    =======================
    ==============================================================================
    ============================================================================*//*


    */
/*============================================================================
    ==============================================================================
    ===========================     ITEM START       =============================
    ==============================================================================
    ============================================================================*//*


    private int maxDyeAllowed = 64;
    private int dyeToAmountConversion = 3;

    public boolean isItemAllowedInTable(ItemStack stackIncoming)
    {
        if (stackIncoming.getItem().equals(Items.RED_DYE) && getRedDyeValue <=61) return true;
        if (stackIncoming.getItem().equals(Items.GREEN_DYE) && getGreenDyeValue <=61) return true;
        if (stackIncoming.getItem().equals(Items.BLUE_DYE) && getBlueDyeValue <=61) return true;

        return false;
    }





    */
/*============================================================================
    ==============================================================================
    ===========================      ITEM END        =============================
    ==============================================================================
    ============================================================================*//*


    */
/*============================================================================
    ==============================================================================
    ===========================   CRAFTING START     =============================
    ==============================================================================
    ============================================================================*//*


    public boolean hasEnoughToCraftDye(int red, int green, int blue)
    {
        boolean redBool = hasItemInTable(0) && (getItemInTable(0).getCount() >= red);
        boolean greenBool = hasItemInTable(1) && (getItemInTable(1).getCount() >= green);
        boolean blueBool = hasItemInTable(2) && (getItemInTable(2).getCount() >= blue);

        return false;
    }

    public int calculateFuelModifiedDuration()
    {
        int fuelDuration = getStoredDust().getDustAmount();
        double durationMod = 0;
        if(isAcceptedModifierItem(getModifierStack())) durationMod = getProcessResultModifierDuration(getRecipeModifier(getLevel(),getModifierStack()));
        if(durationMod > 0)
        {
            return (int)((double)fuelDuration * durationMod);
        }
        else if(durationMod < 0)
        {
            double modifierAbs = Math.abs(durationMod);
            return (int)((double)fuelDuration / modifierAbs);
        }
        else if(fuelDuration > 0)
        {
            return fuelDuration;
        }

        return 0;
    }

    public ItemStack setupCraftedScroll()
    {
        return setupCraftedScroll(1);
    }

    public ItemStack setupCraftedScroll(int count)
    {
        ItemStack returnedStack = ItemStack.EMPTY;
        if(hasEnoughToCraftScroll())
        {
            int duration = calculateFuelModifiedDuration()/count;
            if(duration>=1)
            {
                returnedStack = new ItemStack(DeferredRegisterItems.EFFECT_SCROLL.get(),count);
                ItemStack crystalStack = new ItemStack(DeferredRegisterItems.COLORED_CRYSTAL.get(),count);
                //MobEffect getEffect = calculateMobEffect();
                MobEffect getEffect = MowLibEffectUtils.getEffectForColor(getLevel(), crystalStack, hasCorruption(), getStoredDust().getDustColor());
                int ticksPerDust = EffectScrollsConfig.COMMON.normalEffectTicksDurationPerDust.get();
                int durationModified = duration*ticksPerDust;
                if(getEffect.isInstantenous())
                {
                    int instantDurationTicks = MowLibEffectUtils.getInstantDuration(getLevel(), crystalStack, hasCorruption(),getStoredDust().getDustColor());
                    int dustPerBurst = EffectScrollsConfig.COMMON.instaEffectDustPerEffectBurst.get();
                    int instantMod = duration/dustPerBurst;
                    durationModified = instantDurationTicks * ((instantMod<=0)?(1):(instantMod));
                }
                MobEffectInstance newInstance = new MobEffectInstance(getEffect,durationModified,calculateModifiedPotency(),false,false,true);

                if(returnedStack.getItem() instanceof EffectItemBase)
                {
                    EffectItemBase scroll = (EffectItemBase)returnedStack.getItem();
                    scroll.setEffectToItem(returnedStack, newInstance);
                    MowLibColorReference.addColorToItemStack(returnedStack,getStoredDust().getDustColor());
                }
            }
        }

        return returnedStack;
    }

    public ItemStack craftScrolls(int count)
    {
        int getPaper = getItemInTable(0).getCount();
        int getNuggs = getItemInTable(1).getCount();
        ItemStack getModifier = getItemInTable(2);

        int allowedCount = (count > getPaper)?(getPaper):(count);
        allowedCount = (allowedCount > getNuggs)?(getNuggs):(allowedCount);
        if(!getModifier.isEmpty())allowedCount = (allowedCount > getModifier.getCount())?(getModifier.getCount()):(allowedCount);

        if( this.getScrollCrafted.getItem() instanceof ScrollBase scroll)
        {
            MobEffectInstance effect = scroll.getEffectFromItem(this.getScrollCrafted);
            if(effect.getEffect().isInstantenous())
            {
                int scrollSize = scroll.getMaxStackSize(this.getScrollCrafted);
                allowedCount = (allowedCount > scrollSize)?(scrollSize):(allowedCount);
            }
        }

        ItemStack returnerStack = setupCraftedScroll(allowedCount);
        if(!returnerStack.isEmpty())
        {
            removeItemInTable(0, allowedCount);
            removeItemInTable(1, allowedCount);
            removeItemInTable(2, allowedCount);
            removeDust(getStoredDust().getDustAmount(), IDustHandler.DustAction.EXECUTE);
            update();
            return returnerStack;
        }
        return ItemStack.EMPTY;
    }



    private ItemStack getScrollCraftingOutput()
    {
        if(isFullyRepaired())
        {
            if(hasEnoughToCraftScroll())
            {
                this.getScrollCrafted = setupCraftedScroll();
            }
            else
            {
                this.getScrollCrafted = ItemStack.EMPTY;
            }
        }
        else
        {
            this.getScrollCrafted = ItemStack.EMPTY;
        }

        return this.getScrollCrafted;
    }

    public ItemStack getScrollCrafted()
    {
        return this.getScrollCrafted;
    }

    */
/*============================================================================
    ==============================================================================
    ===========================    CRAFTING END      =============================
    ==============================================================================
    ============================================================================*//*


    public void dropInventoryItemsPrivate(Level worldIn, BlockPos pos) {
        IItemHandler ph = tableItemHandler.orElse(null);
        for(int i = 0; i < ph.getSlots(); ++i) {
            MowLibItemUtils.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), ph.getStackInSlot(i));
        }
    }

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);

        CompoundTag invTableTag = p_155245_.getCompound("inv_table");
        tableItemHandler.ifPresent(h -> ((INBTSerializable<CompoundTag>) h).deserializeNBT(invTableTag));

        if(p_155245_.contains("stackCrafted"))
        {
            CompoundTag invTag = p_155245_.getCompound("stackCrafted");
            ItemStackHandler handler = new ItemStackHandler();
            ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);
            this.getScrollCrafted = handler.getStackInSlot(0);
        }

        this.dustCapacity = p_155245_.getInt(MODID + "_dustCapacity");
    }

    @Override
    public CompoundTag save(CompoundTag p_58888_) {
        super.save(p_58888_);

        tableItemHandler.ifPresent(h -> {
            CompoundTag compound = ((INBTSerializable<CompoundTag>) h).serializeNBT();
            p_58888_.put("inv_table", compound);
        });

        CompoundTag compoundStorage = new CompoundTag();
        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(1);
        handler.setStackInSlot(0,this.getScrollCrafted);
        compoundStorage = handler.serializeNBT();
        p_58888_.put("stackCrafted",compoundStorage);

        p_58888_.putInt(MODID + "_dustCapacity", this.getDustCapacity());

        return p_58888_;
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if(this.dustHandler != null) {
            this.dustHandler.invalidate();
        }
    }



}
*/
