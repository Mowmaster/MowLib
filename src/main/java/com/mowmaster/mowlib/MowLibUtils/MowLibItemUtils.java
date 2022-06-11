package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.items.ItemStackHandler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MowLibItemUtils
{
    public static void spawnItemStack(Level worldIn, double x, double y, double z, ItemStack stack) {
        Random RANDOM = new Random();
        double d0 = (double) EntityType.ITEM.getWidth();
        double d1 = 1.0D - d0;
        double d2 = d0 / 2.0D;
        double d3 = Math.floor(x) + RANDOM.nextDouble() * d1 + d2;
        double d4 = Math.floor(y) + RANDOM.nextDouble() * d1;
        double d5 = Math.floor(z) + RANDOM.nextDouble() * d1 + d2;

        while(!stack.isEmpty()) {
            ItemEntity itementity = new ItemEntity(worldIn, d3, d4, d5, stack.split(RANDOM.nextInt(21) + 10));
            float f = 0.05F;
            itementity.lerpMotion(RANDOM.nextGaussian() * 0.05000000074505806D, RANDOM.nextGaussian() * 0.05000000074505806D + 0.20000000298023224D, RANDOM.nextGaussian() * 0.05000000074505806D);
            worldIn.addFreshEntity(itementity);
        }
    }

    public static void damageItem(ItemStack itemToDamage, int damageAmount)
    {
        if(itemToDamage.isDamageableItem())
        {
            int currentDamage = itemToDamage.getDamageValue();
            int maxDamage = itemToDamage.getMaxDamage();
            int damageDifference = maxDamage-currentDamage;
            int damageToSet = damageDifference-damageAmount;

            if(damageDifference>=damageAmount && damageToSet>0)itemToDamage.setDamageValue(currentDamage+damageAmount);
            else
            {
                itemToDamage.shrink(1);
                itemToDamage.setDamageValue(0);
            }
        }
    }

    public static void writeItemStackToNBT(ItemStack stackSaveToThis, List<ItemStack> listToStore)
    {
        CompoundTag compound = new CompoundTag();
        CompoundTag compoundStorage = new CompoundTag();
        if(stackSaveToThis.hasTag()){compound = stackSaveToThis.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listToStore.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listToStore.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put(MODID + "itemStorage",compoundStorage);
        stackSaveToThis.setTag(compound);
    }

    public static List<ItemStack> readItemStackFromNBT(ItemStack storedOnThisStack)
    {
        List<ItemStack> stackList = new ArrayList<>();
        if(storedOnThisStack.hasTag())
        {
            CompoundTag getCompound = storedOnThisStack.getTag();
            if(getCompound.contains(MODID + "itemStorage"))
            {
                CompoundTag invTag = getCompound.getCompound(MODID + "itemStorage");
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
            }
        }

        return stackList;
    }

    public static void writeListStackToNBT(ItemStack stackSaveToThis, List<ItemStack> listToStore, String path)
    {
        CompoundTag compound = new CompoundTag();
        CompoundTag compoundStorage = new CompoundTag();
        if(stackSaveToThis.hasTag()){compound = stackSaveToThis.getTag();}

        ItemStackHandler handler = new ItemStackHandler();
        handler.setSize(listToStore.size());

        for(int i=0;i<handler.getSlots();i++) {handler.setStackInSlot(i,listToStore.get(i));}

        compoundStorage = handler.serializeNBT();
        compound.put(path,compoundStorage);
        stackSaveToThis.setTag(compound);
    }

    public static List<ItemStack> readListStackFromNBT(ItemStack storedOnThisStack, String path)
    {
        List<ItemStack> stackList = new ArrayList<>();
        if(storedOnThisStack.hasTag())
        {
            CompoundTag getCompound = storedOnThisStack.getTag();
            if(getCompound.contains(path))
            {
                CompoundTag invTag = getCompound.getCompound(path);
                ItemStackHandler handler = new ItemStackHandler();
                ((INBTSerializable<CompoundTag>) handler).deserializeNBT(invTag);

                for(int i=0;i<handler.getSlots();i++) {stackList.add(handler.getStackInSlot(i));}
            }
        }

        return stackList;
    }
}
