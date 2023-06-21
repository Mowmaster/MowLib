package com.mowmaster.mowlib.BlockEntities;

import com.mowmaster.mowlib.Capabilities.Dust.CapabilityDust;
import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import com.mowmaster.mowlib.Capabilities.Experience.CapabilityExperience;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import com.mowmaster.mowlib.MowLibUtils.MowLibItemUtils;
import com.mowmaster.mowlib.api.TransportAndStorage.IFilterItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.capability.IFluidHandler;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MowLibBaseOmniStorageBlockEntity extends MowLibBaseFilterableBlockEntity
{
    private ItemStackHandler itemHandler = createItemHandlerPedestal();
    private LazyOptional<IItemHandler> itemCapability = LazyOptional.of(() -> this.itemHandler);
    private IEnergyStorage energyHandler = createEnergyHandler();
    private LazyOptional<IEnergyStorage> energyCapability = LazyOptional.of(() -> this.energyHandler);
    private IFluidHandler fluidHandler = createFluidHandler();
    private LazyOptional<IFluidHandler> fluidCapability = LazyOptional.of(() -> this.fluidHandler);
    private IExperienceStorage experienceHandler = createExperienceHandler();
    private LazyOptional<IExperienceStorage> experienceCapability = LazyOptional.of(() -> this.experienceHandler);
    private IDustHandler dustHandler = createDustHandler();
    private LazyOptional<IDustHandler> dustCapability = LazyOptional.of(() -> this.dustHandler);
    private List<ItemStack> stacksList = new ArrayList<>();
    private MobEffectInstance storedPotionEffect = null;
    private int storedPotionEffectDuration = 0;
    private int storedEnergy = 0;
    private FluidStack storedFluid = FluidStack.EMPTY;
    private int storedExperience = 0;
    private DustMagic storedDust = DustMagic.EMPTY;
    public MowLibBaseOmniStorageBlockEntity getOmniStorage(){return this;}

    public MowLibBaseOmniStorageBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    //Set Max Possible stacks to 64
    public ItemStackHandler createItemHandlerPedestal() {
        return new ItemStackHandler(64) {
            @Override
            public void onLoad() {
                super.onLoad();
            }

            @Override
            public void onContentsChanged(int slot) {
                update();
            }

            @Override
            public boolean isItemValid(int slot, @Nonnull ItemStack stack) {
                //Run filter checks here(slot==0)?(true):(false)
                IFilterItem filter = getIFilterItem();
                if(filter == null || !filter.getFilterDirection().insert())return true;
                return filter.canAcceptItems(getFilterInBlockEntity(),stack);
            }

            @Override
            public int getSlots() {
                return super.getSlots();
            }

            @Override
            public int getStackLimit(int slot, @Nonnull ItemStack stack) {
                //Run filter checks here
                IFilterItem filter = getIFilterItem();
                if(filter == null || !filter.getFilterDirection().insert())return super.getStackLimit(slot, stack);
                return filter.canAcceptCountItems(getOmniStorage(), getFilterInBlockEntity(),  stack.getMaxStackSize(), getSlotSizeLimit(),stack);
                //return super.getStackLimit(slot, stack);
            }

            @Override
            public int getSlotLimit(int slot) {

                //Hopefully never mess with this again
                //Amount of items allowed in the slot --- may use for bibliomania???
                return super.getSlotLimit(slot);
            }

            @Nonnull
            @Override
            public ItemStack getStackInSlot(int slot) {

                return super.getStackInSlot((slot>getSlots())?(0):(slot));
            }

            /*
                Inserts an ItemStack into the given slot and return the remainder. The ItemStack should not be modified in this function!
                Note: This behaviour is subtly different from IFluidHandler.fill(FluidStack, IFluidHandler.FluidAction)
                Params:
                    slot – Slot to insert into.
                    stack – ItemStack to insert. This must not be modified by the item handler.
                    simulate – If true, the insertion is only simulated
                Returns:
                    The remaining ItemStack that was not inserted (if the entire stack is accepted, then return an empty ItemStack).
                    May be the same as the input ItemStack if unchanged, otherwise a new ItemStack.
                    The returned ItemStack can be safely modified after.
            */
            @Nonnull
            @Override
            public ItemStack insertItem(int slot, @Nonnull ItemStack stack, boolean simulate) {
                /*IPedestalFilter filter = getIPedestalFilter();
                if(filter != null)
                {
                    if(filter.getFilterDirection().insert())
                    {
                        int countAllowed = filter.canAcceptCountItems(getPedestal(),stack);
                        ItemStack modifiedStack = stack.copy();
                        super.insertItem((slot>getSlots())?(0):(slot), modifiedStack, simulate);
                        ItemStack returnedStack = modifiedStack.copy();
                        returnedStack.setCount(stack.getCount() - countAllowed);
                        return returnedStack;
                    }
                }*/

                return super.insertItem((slot>getSlots())?(0):(slot), stack, simulate);
            }

            /*
                Extracts an ItemStack from the given slot.
                The returned value must be empty if nothing is extracted,
                otherwise its stack size must be less than or equal to amount and ItemStack.getMaxStackSize().
                Params:
                    slot – Slot to extract from.
                    amount – Amount to extract (may be greater than the current stack's max limit)
                    simulate – If true, the extraction is only simulated
                Returns:
                    ItemStack extracted from the slot, must be empty if nothing can be extracted.
                    The returned ItemStack can be safely modified after, so item handlers should return a new or copied stack.
             */
            @Nonnull
            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {

                IFilterItem filter = getIFilterItem();
                if(filter == null || !filter.getFilterDirection().extract())return super.extractItem((slot>getSlots())?(0):(slot), amount, simulate);
                return super.extractItem((slot>getSlots())?(0):(slot), Math.min(amount, (filter == null)?(amount):((!filter.getFilterDirection().extract())?(amount):(filter.canAcceptCountItems(getOmniStorage(),getFilterInBlockEntity(),  getStackInSlot((slot>getSlots())?(0):(slot)).getMaxStackSize(), getSlotSizeLimit(),getStackInSlot((slot>getSlots())?(0):(slot)))))), simulate);
            }
        };
    }

    public IFluidHandler createFluidHandler() {
        return new IFluidHandler() {

            @Nonnull
            @Override
            public FluidStack getFluidInTank(int i) {
                return storedFluid;
            }

            @Override
            public int getTankCapacity(int tank) {
                return 0;
            }

            @Override
            public int getTanks() {
                //Technically we dont use the tanks thing, but we'll pretend and hope it doesnt break...
                return 1;
            }

            @Nonnull
            @Override
            public FluidStack drain(FluidStack resource, FluidAction action) {
                if (resource.isEmpty() || !resource.isFluidEqual(storedFluid)) {
                    return FluidStack.EMPTY;
                }
                return drain(resource.getAmount(), action);
            }

            @Nonnull
            @Override
            public FluidStack drain(int maxDrain, FluidAction fluidAction) {
                IFilterItem filter = getIFilterItem();
                int maxDrainPostFilter = (filter != null && filter.getFilterDirection().extract()) ? Math.min(filter.canAcceptCountFluids(getOmniStorage(),getFilterInBlockEntity(),getFluidCapacity(),spaceForFluid(),storedFluid), maxDrain) : maxDrain;
                int fluidDrained = Math.min(maxDrainPostFilter, storedFluid.getAmount());
                // `storedFluid.shrink` can reduce `storedFluid` to an empty stack, so copy it for return before shrinking.
                FluidStack returnFluidStack = new FluidStack(storedFluid, fluidDrained);
                if (fluidAction.execute() && fluidDrained > 0) {
                    storedFluid.shrink(fluidDrained);
                    update();
                }
                return returnFluidStack;
            }

            @Override
            public boolean isFluidValid(int i, @Nonnull FluidStack fluidStack) {
                IFilterItem filter = getIFilterItem();
                if (filter == null || !filter.getFilterDirection().insert()) {
                    return storedFluid.isEmpty() || storedFluid.isFluidEqual(fluidStack);
                } else {
                    return filter.canAcceptFluids(getFilterInBlockEntity(), fluidStack);
                }
            }

            @Override
            public int fill(FluidStack fluidStack, FluidAction fluidAction) {
                if (fluidStack.isEmpty() || !isFluidValid(0, fluidStack) || (!storedFluid.isEmpty() && !storedFluid.isFluidEqual(fluidStack))) {
                    return 0;
                }

                IFilterItem filter = getIFilterItem();
                int fluidAmountPostFilter = (filter != null && filter.getFilterDirection().insert()) ? Math.min(filter.canAcceptCountFluids(getOmniStorage(),getFilterInBlockEntity(),getFluidCapacity(),spaceForFluid(),fluidStack), fluidStack.getAmount()) : fluidStack.getAmount();
                int amountFilled = Math.min(getTankCapacity(0) - storedFluid.getAmount(), fluidAmountPostFilter);
                if (fluidAction.execute()) {
                    if (storedFluid.isEmpty()) {
                        storedFluid = new FluidStack(fluidStack, amountFilled);
                    } else {
                        storedFluid.grow(amountFilled);
                    }
                    update();
                }
                return amountFilled;
            }
        };
    }

    public IEnergyStorage createEnergyHandler() {
        return new IEnergyStorage() {

            /*
            Adds energy to the storage. Returns quantity of energy that was accepted.
            Params:
                maxReceive – Maximum amount of energy to be inserted.
                simulate – If TRUE, the insertion will only be simulated.
            Returns:
                Amount of energy that was (or would have been, if simulated) accepted by the storage.
             */
            @Override
            public int receiveEnergy(int maxReceive, boolean simulate) {
                if (!canReceive())
                    return 0;

                IFilterItem filter = getIFilterItem();
                int maxReceivePostFilter = (filter != null && filter.getFilterDirection().insert()) ? filter.canAcceptCountEnergy(getOmniStorage(),getFilterInBlockEntity(),getEnergyCapacity(),spaceForEnergy(),maxReceive): maxReceive;
                int energyReceived = Math.min(getMaxEnergyStored() - getEnergyStored(), maxReceivePostFilter);
                if (!simulate)
                    storedEnergy += energyReceived;
                update();
                return energyReceived;
            }

            /*
            Removes energy from the storage. Returns quantity of energy that was removed.
            Params:
                maxExtract – Maximum amount of energy to be extracted.
                simulate – If TRUE, the extraction will only be simulated.
            Returns:
                Amount of energy that was (or would have been, if simulated) extracted from the storage.
             */
            @Override
            public int extractEnergy(int maxExtract, boolean simulate) {
                if (!canExtract())
                    return 0;

                IFilterItem filter = getIFilterItem();
                int maxExtractPostFilter = (filter != null && filter.getFilterDirection().extract()) ? filter.canAcceptCountEnergy(getOmniStorage(),getFilterInBlockEntity(),getEnergyCapacity(),spaceForEnergy(),maxExtract): maxExtract;
                int energyExtracted = Math.min(storedEnergy, maxExtractPostFilter);
                if (!simulate)
                    storedEnergy -= energyExtracted;
                return energyExtracted;
            }

            @Override
            public int getEnergyStored() {
                return storedEnergy;
            }

            @Override
            public int getMaxEnergyStored() {
                return 0;
            }

            @Override
            public boolean canExtract() {
                if(hasEnergy())
                {
                    IFilterItem filter = getIFilterItem();
                    if(filter == null || !filter.getFilterDirection().extract())return true;
                    return filter.canAcceptEnergy(getFilterInBlockEntity(),1);
                }
                return false;
            }

            @Override
            public boolean canReceive() {
                if(hasSpaceForEnergy())
                {
                    IFilterItem filter = getIFilterItem();
                    if(filter == null || !filter.getFilterDirection().insert())return true;
                    return filter.canAcceptEnergy(getFilterInBlockEntity(),1);
                }

                return false;
            }
        };
    }

    public IExperienceStorage createExperienceHandler() {
        return new IExperienceStorage() {

            @Override
            public int receiveExperience(int maxReceive, boolean simulate) {
                if (!canReceive())
                    return 0;

                int spaceAvailable = getMaxExperienceStored() - getExperienceStored();
                IFilterItem filter = getIFilterItem();
                int maxReceivePostFilter = (filter != null && filter.getFilterDirection().insert()) ? filter.canAcceptCountExperience(getOmniStorage(),getFilterInBlockEntity(),getExperienceCapacity(),spaceForExperience(),maxReceive): maxReceive;
                int experienceReceived = Math.min(spaceAvailable, maxReceivePostFilter);
                if (!simulate)
                    storedExperience += experienceReceived;
                update();
                return experienceReceived;
            }

            @Override
            public int extractExperience(int maxExtract, boolean simulate) {
                if (!canExtract())
                    return 0;


                IFilterItem filter = getIFilterItem();
                int maxExtractPostFilter = (filter != null && filter.getFilterDirection().extract()) ? filter.canAcceptCountExperience(getOmniStorage(),getFilterInBlockEntity(),getExperienceCapacity(),spaceForExperience(),maxExtract) : maxExtract;
                int experienceExtracted = Math.min(storedExperience, maxExtractPostFilter);
                if (!simulate)
                    storedExperience -= experienceExtracted;
                return experienceExtracted;
            }

            @Override
            public int getExperienceStored() { return storedExperience; }

            @Override
            public int getMaxExperienceStored() {
                return 0;
            }

            @Override
            public boolean canExtract() {
                if(hasExperience())
                {

                    IFilterItem filter = getIFilterItem();
                    if(filter == null || !filter.getFilterDirection().extract())return true;
                    return filter.canAcceptExperience(getFilterInBlockEntity(),1);
                }
                return false;
            }

            @Override
            public boolean canReceive() {
                if(hasSpaceForExperience())
                {

                    IFilterItem filter = getIFilterItem();
                    if(filter == null || !filter.getFilterDirection().insert())return true;
                    return filter.canAcceptExperience(getFilterInBlockEntity(),1);
                }

                return false;
            }
        };
    }

    public IDustHandler createDustHandler() {
        return new IDustHandler() {
            protected void onContentsChanged() {
                update();
            }

            @Override
            public int getTanks() {
                return 1;
            }

            @NotNull
            @Override
            public DustMagic getDustMagicInTank(int tank) {
                return storedDust;
            }

            @Override
            public int getTankCapacity(int tank) {
                return 0;
            }

            @Override
            public boolean isDustValid(int tank, @NotNull DustMagic dustIn) {

                IFilterItem filter = getIFilterItem();
                if(filter == null || !filter.getFilterDirection().insert())return storedDust.isDustEqualOrEmpty(dustIn);
                return filter.canAcceptDust(getFilterInBlockEntity(),dustIn);
            }

            @Override
            public int fill(DustMagic dust, DustAction action) {
                if (dust.isEmpty() || !isDustValid(0,dust) || !storedDust.isDustEqual(dust)) {
                    return 0;
                }


                IFilterItem filter = getIFilterItem();
                int dustAmountPostFilter =  (filter != null && filter.getFilterDirection().insert()) ? Math.min(filter.canAcceptCountDust(getOmniStorage(),getFilterInBlockEntity(),getDustCapacity(),spaceForDust(),dust), dust.getDustAmount()) : dust.getDustAmount();
                if (storedDust.isEmpty()) {
                    int amountFilled = Math.min(getTankCapacity(0), dustAmountPostFilter);
                    if (!action.simulate()) {
                        storedDust = new DustMagic(dust.getDustColor(), amountFilled);
                        onContentsChanged();
                    }
                    return amountFilled;
                } else {
                    int amountFilled = Math.min(Math.min(getTankCapacity(0) - storedDust.getDustAmount(), dustAmountPostFilter), dust.getDustAmount());

                    if (!action.simulate() && amountFilled > 0) {
                        storedDust.grow(amountFilled);
                        onContentsChanged();
                    }
                    return amountFilled;
                }
            }

            @NotNull
            @Override
            public DustMagic drain(DustMagic dust, DustAction action) {
                if (dust.isEmpty() || !dust.isDustEqual(storedDust)) {
                    return new DustMagic(-1, 0);
                }
                return drain(dust.getDustAmount(), action);
            }

            @NotNull
            @Override
            public DustMagic drain(int maxDrain, DustAction action) {

                IFilterItem filter = getIFilterItem();
                int maxDrainPostFilter = (filter != null && filter.getFilterDirection().extract()) ? Math.min(filter.canAcceptCountDust(getOmniStorage(),getFilterInBlockEntity(),getDustCapacity(),spaceForDust(),storedDust), maxDrain) : maxDrain;
                int amountDrained = Math.min(storedDust.getDustAmount(), maxDrainPostFilter);
                DustMagic magic = new DustMagic(storedDust.getDustColor(), amountDrained);
                if (action.execute() && amountDrained > 0) {
                    if (amountDrained >= storedDust.getDustAmount()) {
                        storedDust.setDustAmount(0);
                        storedDust.setDustColor(-1);
                    } else {
                        storedDust.shrink(amountDrained);
                    }
                    onContentsChanged();
                }
                return magic;
            }
        };
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        if (cap == ForgeCapabilities.ITEM_HANDLER) {
            return itemCapability.cast();
        }
        if ((cap == ForgeCapabilities.ENERGY)) {
            return energyCapability.cast();
        }
        if ((cap == ForgeCapabilities.FLUID_HANDLER)) {
            return fluidCapability.cast();
        }
        if ((cap == CapabilityExperience.EXPERIENCE)) {
            return experienceCapability.cast();
        }
        if ((cap == CapabilityDust.DUST_HANDLER)) {
            return dustCapability.cast();
        }
        return super.getCapability(cap, side);
    }

    public void dropInventoryItems(Level worldIn, BlockPos pos) {
        MowLibItemUtils.dropInventoryItems(worldIn, pos, itemHandler);
    }

    public List<ItemStack> dropInventoryItemsList() {
        List<ItemStack> returner = new ArrayList<>();
        for(int i = 0; i < itemHandler.getSlots(); ++i) {
            returner.add(itemHandler.getStackInSlot(i));
        }

        return returner;
    }

    public void dropLiquidsInWorld(Level worldIn, BlockPos pos) {
        FluidStack inTank = fluidHandler.getFluidInTank(0);
        if (inTank.getAmount()>0) {
            /*ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_FLUID.get());
            if(toDrop.getItem() instanceof BaseFluidBulkStorageItem droppedItemFluid) {
                droppedItemFluid.setFluidStack(toDrop,inTank);
            }
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);*/
        }
    }

    public void removeEnergyFromBrokenPedestal(Level worldIn, BlockPos pos) {
        if(energyHandler.getEnergyStored()>0)
        {
            /*ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_ENERGY.get());
            if(toDrop.getItem() instanceof BaseEnergyBulkStorageItem droppedItemEnergy)
            {
                droppedItemEnergy.setEnergy(toDrop,energyHandler.getEnergyStored());
            }
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);*/
        }
    }

    public void dropXPInWorld(Level worldIn, BlockPos pos) {
        if(experienceHandler.getExperienceStored()>0)
        {
            /*ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_XP.get());
            if(toDrop.getItem() instanceof BaseXpBulkStorageItem droppedItemEnergy)
            {
                droppedItemEnergy.setXp(toDrop,experienceHandler.getExperienceStored());
                //System.out.println("stored xp: "+ droppedItemEnergy.getXp(toDrop));
            }
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);*/
        }
    }

    public void dropDustInWorld(Level worldIn, BlockPos pos) {
        if(!dustHandler.getDustMagicInTank(0).isEmpty())
        {
            /*ItemStack toDrop = new ItemStack(DeferredRegisterItems.MECHANICAL_STORAGE_DUST.get());
            DustMagic.setDustMagicInStack(toDrop,storedDust);
            MowLibItemUtils.spawnItemStack(worldIn,pos.getX(),pos.getY(),pos.getZ(),toDrop);*/
        }
    }

    /*============================================================================
    ==============================================================================
    ===========================     ITEM START       =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasItem() {
        int firstPartialOrNonEmptySlot = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if(stackInSlot.getCount() < stackInSlot.getMaxStackSize() || stackInSlot.isEmpty()) {
                firstPartialOrNonEmptySlot = i;
                break;
            }
        }

        return !itemHandler.getStackInSlot(firstPartialOrNonEmptySlot).isEmpty();
    }

    public Optional<Integer> maybeFirstNonEmptySlot() {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if(!itemHandler.getStackInSlot(i).isEmpty()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public boolean hasItemFirst() {
        return maybeFirstNonEmptySlot().isPresent();
    }

    public Optional<Integer> maybeLastNonEmptySlot() {
        for (int i = itemHandler.getSlots() - 1; i >= 0; i--) {
            if(!itemHandler.getStackInSlot(i).isEmpty()) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public Optional<Integer> maybeFirstSlotWithSpaceForMatchingItem(ItemStack stackToMatch) {
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            ItemStack stackInSlot = itemHandler.getStackInSlot(i);
            if (stackInSlot.isEmpty() || (stackInSlot.getCount() < stackInSlot.getMaxStackSize() && ItemHandlerHelper.canItemStacksStack(stackInSlot, stackToMatch))) {
                return Optional.of(i);
            }
        }
        return Optional.empty();
    }

    public boolean hasSpaceForItem(ItemStack stackToMatch) {
        return maybeFirstSlotWithSpaceForMatchingItem(stackToMatch).isPresent();
    }

    public ItemStack getItemInPedestal() {
        return maybeFirstNonEmptySlot().map(slot -> itemHandler.getStackInSlot(slot)).orElse(ItemStack.EMPTY);
    }

    public ItemStack getMatchingItemInPedestalOrEmptySlot(ItemStack stackToMatch) {
        return maybeFirstSlotWithSpaceForMatchingItem(stackToMatch).map(slot -> itemHandler.getStackInSlot(slot)).orElse(ItemStack.EMPTY);

    }

    public ItemStack getItemInPedestalFirst() {
        return maybeFirstNonEmptySlot().map(slot -> itemHandler.getStackInSlot(slot)).orElse(ItemStack.EMPTY);

    }

    public int getPedestalSlots() { return itemHandler.getSlots(); }

    public List<ItemStack> getItemStacks() {
        List<ItemStack> listed = new ArrayList<>();
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if (!itemHandler.getStackInSlot(i).isEmpty()) {
                listed.add(itemHandler.getStackInSlot(i));
            }
        }
        return listed;
    }

    public ItemStack getItemInPedestal(int slot) {
        if (itemHandler.getSlots() > slot) {
            return itemHandler.getStackInSlot(slot);
        } else {
            return ItemStack.EMPTY;
        }
    }

    public ItemStack removeItem(int numToRemove, boolean simulate) {
        return maybeLastNonEmptySlot().map(slot -> itemHandler.extractItem(slot, numToRemove, simulate)).orElse(ItemStack.EMPTY);
    }

    public ItemStack removeItemStack(ItemStack stackToRemove, boolean simulate) {
        int matchingSlotNumber = 0;
        for (int i = 0; i < itemHandler.getSlots(); i++) {
            if(ItemHandlerHelper.canItemStacksStack(itemHandler.getStackInSlot(i), stackToRemove)) {
                matchingSlotNumber = i;
                break;
            }
        }
        return itemHandler.extractItem(matchingSlotNumber, stackToRemove.getCount(), simulate);
    }

    public ItemStack removeItem(boolean simulate) {
        return maybeLastNonEmptySlot().map(slot -> {
            ItemStack stack = itemHandler.extractItem(slot, itemHandler.getStackInSlot(slot).getCount(), simulate);
            //update();
            return stack;
        }).orElse(ItemStack.EMPTY);
    }

    //If resulting insert stack is empty it means the full stack was inserted
    public boolean addItem(ItemStack itemFromBlock, boolean simulate) {
        return addItemStack(itemFromBlock, simulate).isEmpty();
    }

    //Return result not inserted, if all inserted return empty stack
    public ItemStack addItemStack(ItemStack itemFromBlock, boolean simulate) {
        return maybeFirstSlotWithSpaceForMatchingItem(itemFromBlock).map(slot -> {
            if (itemHandler.isItemValid(slot, itemFromBlock)) {
                ItemStack returner = itemHandler.insertItem(slot, itemFromBlock.copy(), simulate);
                if (!simulate) update();
                return returner;
            }
            return itemFromBlock;
        }).orElse(itemFromBlock);
    }

    public int getSlotSizeLimit() {
        return maybeFirstNonEmptySlot().map(slot -> itemHandler.getSlotLimit(slot)).orElse(itemHandler.getSlotLimit(0));
    }

    /*============================================================================
    ==============================================================================
    ===========================      ITEM END        =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    FLUID  START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasFluid() { return !fluidHandler.getFluidInTank(0).isEmpty(); }

    public FluidStack getStoredFluid() { return fluidHandler.getFluidInTank(0); }

    public int getFluidCapacity() { return fluidHandler.getTankCapacity(0); }

    public int spaceForFluid()
    {
        return getFluidCapacity() - getStoredFluid().getAmount();
    }

    public boolean canAcceptFluid(FluidStack fluidStackIn) { return fluidHandler.isFluidValid(0,fluidStackIn); }

    public FluidStack removeFluid(FluidStack fluidToRemove, IFluidHandler.FluidAction action) { return fluidHandler.drain(fluidToRemove, action); }

    public FluidStack removeFluid(int fluidAmountToRemove, IFluidHandler.FluidAction action) { return fluidHandler.drain(new FluidStack(getStoredFluid().getFluid(),fluidAmountToRemove,getStoredFluid().getTag()),action); }

    public int addFluid(FluidStack fluidStackIn, IFluidHandler.FluidAction fluidAction) { return fluidHandler.fill(fluidStackIn,fluidAction); }

    /*============================================================================
    ==============================================================================
    ===========================     FLUID  END       =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    ENERGY START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasEnergy() { return energyHandler.getEnergyStored() > 0; }

    public boolean hasSpaceForEnergy() { return spaceForEnergy() > 0; }

    public int spaceForEnergy() { return getEnergyCapacity() - getStoredEnergy(); }

    public int getEnergyCapacity() { return energyHandler.getMaxEnergyStored(); }

    public int getStoredEnergy() { return energyHandler.getEnergyStored(); }

    public int addEnergy(int amountIn, boolean simulate) { return energyHandler.receiveEnergy(amountIn,simulate); }

    public int removeEnergy(int amountOut, boolean simulate) { return energyHandler.extractEnergy(amountOut,simulate); }

    public boolean canAcceptEnergy() { return energyHandler.canReceive(); }

    public boolean canSendEnergy() { return energyHandler.canExtract(); }

    /*============================================================================
    ==============================================================================
    ===========================     ENERGY END       =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================  EXPERIENCE START    =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasExperience() { return experienceHandler.getExperienceStored() > 0; }

    public boolean hasSpaceForExperience() { return spaceForExperience() > 0; }

    public int spaceForExperience()
    {
        return getExperienceCapacity() - getStoredExperience();
    }

    public int getExperienceCapacity() { return experienceHandler.getMaxExperienceStored(); }

    public int getStoredExperience() { return experienceHandler.getExperienceStored(); }

    public int addExperience(int amountIn, boolean simulate) { return experienceHandler.receiveExperience(amountIn, simulate); }

    public int removeExperience(int amountOut, boolean simulate) { return experienceHandler.extractExperience(amountOut,simulate); }

    public boolean canAcceptExperience() { return experienceHandler.canReceive(); }

    public boolean canSendExperience() { return experienceHandler.canExtract(); }

    /*============================================================================
    ==============================================================================
    ===========================   EXPERIENCE END     =============================
    ==============================================================================
    ============================================================================*/



    /*============================================================================
    ==============================================================================
    ===========================    DUST   START      =============================
    ==============================================================================
    ============================================================================*/

    public boolean hasDust() { return !dustHandler.getDustMagicInTank(0).isEmpty(); }

    public DustMagic getStoredDust() {
        if (hasDust()) {
            return dustHandler.getDustMagicInTank(0);
        } else {
            return DustMagic.EMPTY;
        }
    }

    public int getDustCapacity() { return dustHandler.getTankCapacity(0); }

    public int spaceForDust() { return getDustCapacity()  -getStoredDust().getDustAmount(); }

    public boolean canAcceptDust(DustMagic dustMagicIn) { return dustHandler.isDustValid(0,dustMagicIn); }

    public DustMagic removeDust(DustMagic dustMagicToRemove, IDustHandler.DustAction action) {
        update();
        return dustHandler.drain(dustMagicToRemove,action);
    }

    public DustMagic removeDust(int dustAmountToRemove, IDustHandler.DustAction action) {
        update();
        return dustHandler.drain(new DustMagic(getStoredDust().getDustColor(),dustAmountToRemove),action);
    }

    public int addDust(DustMagic dustMagicIn, IDustHandler.DustAction action) {
        update();
        return dustHandler.fill(dustMagicIn,action);
    }

    /*============================================================================
    ==============================================================================
    ===========================     DUST   END       =============================
    ==============================================================================
    ============================================================================*/

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
        CompoundTag invTag = p_155245_.getCompound("inv");
        itemHandler.deserializeNBT(invTag);
        this.storedEnergy = p_155245_.getInt("storedEnergy");
        this.storedFluid = FluidStack.loadFluidStackFromNBT(p_155245_.getCompound("storedFluid"));
        this.storedExperience = p_155245_.getInt("storedExperience");
        this.storedDust = DustMagic.getDustMagicInTag(p_155245_);
        this.storedPotionEffect = (MobEffectInstance.load(p_155245_)!=null)?(MobEffectInstance.load(p_155245_)):(null);
        this.storedPotionEffectDuration = p_155245_.getInt("storedEffectDuration");
    }

    @Override
    public CompoundTag save(CompoundTag p_58888_) {
        super.save(p_58888_);
        p_58888_.put("inv", itemHandler.serializeNBT());
        p_58888_.putInt("storedEnergy",storedEnergy);
        p_58888_.put("storedFluid",storedFluid.writeToNBT(new CompoundTag()));
        p_58888_.putInt("storedExperience",storedExperience);
        if(storedPotionEffect!=null)storedPotionEffect.save(p_58888_);
        p_58888_.putInt("storedEffectDuration",storedPotionEffectDuration);

        return DustMagic.setDustMagicInTag(p_58888_,this.storedDust);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        itemCapability.invalidate();
        energyCapability.invalidate();
        fluidCapability.invalidate();
        experienceCapability.invalidate();
        dustCapability.invalidate();
    }
}
