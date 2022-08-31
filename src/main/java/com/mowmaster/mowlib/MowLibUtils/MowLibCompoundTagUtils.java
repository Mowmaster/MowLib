package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MowLibCompoundTagUtils
{
    public static CompoundTag writeItemStackListToNBT(String ModID, @Nullable CompoundTag inputNBT, List<ItemStack> listIn)
    {
        CompoundTag compound = (inputNBT != null)?(inputNBT):(new CompoundTag());
        CompoundTag compoundStorage = new CompoundTag();

        if(listIn.size()>0)
        {
            ItemStackHandler handler = new ItemStackHandler();
            handler.setSize(listIn.size());
            for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}
            compoundStorage = handler.serializeNBT();
        }
        compound.put(ModID + "_stacksStored",compoundStorage);
        return compound;
    }

    public static List<ItemStack> readItemStackListFromNBT(String ModID, CompoundTag inputNBT)
    {
        if(inputNBT == null) return null;

        if(inputNBT.contains(ModID + "_stacksStored"))
        {
            List<ItemStack> stackList = new ArrayList<>();
            CompoundTag invTag = inputNBT.getCompound(ModID + "_stacksStored");
            ItemStackHandler handler = new ItemStackHandler();
            ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

            for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
            return stackList;
        }

        return null;
    }

    public static void removeItemStackFromNBT(String ModID, CompoundTag inputNBT) { if(inputNBT.contains(ModID + "_stacksStored")) { inputNBT.remove(ModID + "_stacksStored"); } }

    public static CompoundTag writeFluidStackToNBT(String ModID, @Nullable CompoundTag inputNBT, FluidStack fluidStack) {
        CompoundTag compound = inputNBT != null ? inputNBT : new CompoundTag();
        if (fluidStack.isEmpty()) {
            return compound;
        } else {
            compound.put(ModID + "_storedFluid", fluidStack.writeToNBT(new CompoundTag()));
            return compound;
        }
    }

    public static FluidStack readFluidStackFromNBT(String ModID, CompoundTag inputNBT) {
        FluidStack fluidStack = FluidStack.EMPTY;
        if (inputNBT.contains(ModID + "_storedFluid")) {
            return FluidStack.loadFluidStackFromNBT(inputNBT.getCompound(ModID + "_storedFluid"));
        } else {
            return fluidStack;
        }
    }

    public static void removeFluidStackFromNBT(String ModID, CompoundTag inputNBT) { if(inputNBT.contains(ModID + "_storedFluid")) { inputNBT.remove(ModID + "_storedFluid"); } }

    public static CompoundTag writeIntegerToNBT(String ModID, @Nullable CompoundTag inputNBT, int input, String intName) {
        CompoundTag compound = inputNBT != null ? inputNBT : new CompoundTag();
        compound.putInt(ModID + intName, input);
        return compound;
    }

    public static int readIntegerFromNBT(String ModID, CompoundTag inputNBT, String intName)
    {
        if(inputNBT.contains(ModID + intName))
        {
            return inputNBT.getInt(ModID + intName);
        }
        return 0;
    }

    public static void removeIntegerFromNBT(String ModID, CompoundTag inputNBT, String intName) { if(inputNBT.contains(ModID + intName)) { inputNBT.remove(ModID + intName); } }
}
