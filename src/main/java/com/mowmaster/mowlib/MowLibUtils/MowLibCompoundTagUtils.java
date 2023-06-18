package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

public class MowLibCompoundTagUtils
{
    /*===============================================================
    =================================================================
    =================================================================
    =====                      Item Handler                     =====
    =================================================================
    =================================================================
    ===============================================================*/
    public static CompoundTag writeItemHandlerToNBT(String ModID, @Nullable CompoundTag inputNBT, LazyOptional<ItemStackHandler> handler, String identifier)
    {
        CompoundTag compound = (inputNBT != null)?(inputNBT):(new CompoundTag());
        handler.ifPresent(h -> {
            CompoundTag compoundStorage = ((INBTSerializable<CompoundTag>) h).serializeNBT();
            compound.put(ModID + identifier, compoundStorage);
        });

        return compound;
    }

    public static List<ItemStack> readItemHandlerFromNBT(String ModID, CompoundTag inputNBT, String identifier)
    {
        List<ItemStack> stackList = new ArrayList<>();
        if(inputNBT == null) return stackList;
        if(inputNBT.contains(ModID + identifier))
        {
            CompoundTag invTag = inputNBT.getCompound(ModID + identifier);
            ItemStackHandler handler = new ItemStackHandler();
            ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

            for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
            return stackList;
        }

        return stackList;
    }

    //Returning an item handler proved to be too much thinking,
    //so ill do most the leg work then have the BE use the tag to do the rest of the work to find the handler.
    public static CompoundTag readItemHandlerTagFromNBT(String ModID, CompoundTag inputNBT, String identifier)
    {
        if(inputNBT == null) return null;
        if(inputNBT.contains(ModID + identifier))
        {
            CompoundTag invTag = inputNBT.getCompound(ModID + identifier);
            return invTag;
        }

        return null;
    }



    /*===============================================================
    =================================================================
    =================================================================
    =====                      Stack List                       =====
    =================================================================
    =================================================================
    ===============================================================*/

    public static CompoundTag writeItemStackListToNBT(String ModID, @Nullable CompoundTag inputNBT, List<ItemStack> listIn, String identifier)
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
        compound.put(ModID + identifier,compoundStorage);
        return compound;
    }

    public static List<ItemStack> readItemStackListFromNBT(String ModID, CompoundTag inputNBT, String identifier)
    {
        if(inputNBT == null) return null;

        if(inputNBT.contains(ModID + identifier))
        {
            List<ItemStack> stackList = new ArrayList<>();
            CompoundTag invTag = inputNBT.getCompound(ModID + identifier);
            ItemStackHandler handler = new ItemStackHandler();
            ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

            for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
            return stackList;
        }

        return null;
    }

    public static CompoundTag writeItemStackToNBT(String ModID, @Nullable CompoundTag inputNBT, ItemStack stackIn, String identifier)
    {
        CompoundTag compound = (inputNBT != null)?(inputNBT):(new CompoundTag());
        CompoundTag compoundStorage = new CompoundTag();

        List<ItemStack> listIn = new ArrayList<>();
        listIn.add(stackIn);
        if(!stackIn.isEmpty())
        {
            ItemStackHandler handler = new ItemStackHandler();
            handler.setSize(listIn.size());
            for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listIn.get(i));}
            compoundStorage = handler.serializeNBT();
        }
        compound.put(ModID + identifier,compoundStorage);
        return compound;
    }

    public static ItemStack readItemStackFromNBT(String ModID, CompoundTag inputNBT, String identifier)
    {
        ItemStack returnerStack = ItemStack.EMPTY;
        if(inputNBT == null) return returnerStack;
        if(inputNBT.contains(ModID + identifier))
        {
            List<ItemStack> stackList = new ArrayList<>();
            CompoundTag invTag = inputNBT.getCompound(ModID + identifier);
            ItemStackHandler handler = new ItemStackHandler();
            ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

            for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
            return stackList.stream().findFirst().orElse(ItemStack.EMPTY);
        }

        return returnerStack;
    }



    /*===============================================================
    =================================================================
    =================================================================
    =====                        Fluids                         =====
    =================================================================
    =================================================================
    ===============================================================*/
    public static CompoundTag writeFluidStackToNBT(String ModID, @Nullable CompoundTag inputNBT, FluidStack fluidStack, String identifier) {
        CompoundTag compound = inputNBT != null ? inputNBT : new CompoundTag();
        if (fluidStack.isEmpty()) {
            return compound;
        } else {
            compound.put(ModID + identifier, fluidStack.writeToNBT(new CompoundTag()));
            return compound;
        }
    }

    public static FluidStack readFluidStackFromNBT(String ModID, CompoundTag inputNBT, String identifier) {
        FluidStack fluidStack = FluidStack.EMPTY;
        if (inputNBT.contains(ModID + identifier)) {
            return FluidStack.loadFluidStackFromNBT(inputNBT.getCompound(ModID + identifier));
        } else {
            return fluidStack;
        }
    }



    /*===============================================================
    =================================================================
    =================================================================
    =====                        Integer                        =====
    =================================================================
    =================================================================
    ===============================================================*/

    //Is this basically the same as below? why yes, yes it is, but you know what? Sometimes my brain is dumb and needs the extra help...
    public static ItemStack writeIntegerCompoundTagToItemStack(ItemStack stackIn, String ModID, String compoundIntString, int intValue)
    {
        CompoundTag tagOnStack = stackIn.getOrCreateTag();
        tagOnStack.putInt(ModID + compoundIntString,intValue);
        stackIn.setTag(tagOnStack);
        return stackIn;
    }

    public static int getCompoundTagFromItemStack(ItemStack stackIn, String ModID, String compoundIntString)
    {
        CompoundTag tagOnStack = stackIn.getOrCreateTag();
        if(tagOnStack.contains(ModID + compoundIntString))
        {
            int intValue = tagOnStack.getInt(ModID + compoundIntString);
            return intValue;
        }

        return 0;
    }

    public static ItemStack writeIntegerCompoundTagToItemStack(ItemStack stackIn, String compoundIntStringWithModID, int intValue)
    {
        CompoundTag tagOnStack = stackIn.getOrCreateTag();
        tagOnStack.putInt(compoundIntStringWithModID,intValue);
        stackIn.setTag(tagOnStack);
        return stackIn;
    }

    public static int getCompoundTagFromItemStack(ItemStack stackIn, String compoundIntStringWithModID)
    {
        CompoundTag tagOnStack = stackIn.getOrCreateTag();
        if(tagOnStack.contains(compoundIntStringWithModID))
        {
            int intValue = tagOnStack.getInt(compoundIntStringWithModID);
            return intValue;
        }

        return 0;
    }

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



    /*===============================================================
    =================================================================
    =================================================================
    =====                        Boolean                        =====
    =================================================================
    =================================================================
    ===============================================================*/
    public static boolean readBooleanFromNBT(String ModID, CompoundTag tag, String identifier)
    {
        return tag.contains(ModID + identifier) ? tag.getBoolean(ModID + identifier) : false;
    }

    public static CompoundTag writeBooleanToNBT(String ModID, @Nullable CompoundTag tag, boolean value, String identifier) {
        CompoundTag compound = tag != null ? tag : new CompoundTag();
        compound.putBoolean(ModID + identifier, value);
        return compound;
    }



    /*===============================================================
    =================================================================
    =================================================================
    =====                        Strings                        =====
    =================================================================
    =================================================================
    ===============================================================*/
    public static String readStringFromNBT(String ModID, CompoundTag tag, String identifier)
    {
        return tag.contains(ModID + identifier) ? tag.getString(ModID + identifier) : "";
    }

    public static CompoundTag writeStringToNBT(String ModID, @javax.annotation.Nullable CompoundTag tag, String value, String identifier) {
        CompoundTag compound = tag != null ? tag : new CompoundTag();
        compound.putString(ModID + identifier, value);
        return compound;
    }



    /*===============================================================
    =================================================================
    =================================================================
    =====                       BlockPos                        =====
    =================================================================
    =================================================================
    ===============================================================*/



    /*===============================================================
    =================================================================
    =================================================================
    =====               Generic Remove Tag Method               =====
    =================================================================
    =================================================================
    ===============================================================*/
    public static void removeCustomTagFromNBT(String ModID, CompoundTag tag, String identifier) {
        if (tag.contains(ModID + identifier)) {
            tag.remove(ModID + identifier);
        }
    }
}
