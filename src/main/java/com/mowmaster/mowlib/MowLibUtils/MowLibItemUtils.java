package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.RailBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.extensions.IForgeAbstractMinecart;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.IItemHandler;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MowLibItemUtils
{
    public static LazyOptional<IItemHandler> findItemHandlerAtPos(Level world, BlockPos pos, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(ForgeCapabilities.ITEM_HANDLER);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {

                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos.above()), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(cap.isPresent())
                        return cap;
                }
            }
        }
        return LazyOptional.empty();
    }

    public static LazyOptional<IItemHandler> findItemHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IItemHandler> cap = neighbourTile.getCapability(ForgeCapabilities.ITEM_HANDLER, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(ForgeCapabilities.ITEM_HANDLER);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
                System.out.println(list);
                if(!list.isEmpty())
                {
                    LazyOptional<IItemHandler> cap = list.get(world.random.nextInt(list.size())).getCapability(ForgeCapabilities.ITEM_HANDLER);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

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

    public static void dropInventoryItems(Level worldIn, BlockPos pos, IItemHandler h) {
        for(int i = 0; i < h.getSlots(); ++i) {
            MowLibItemUtils.spawnItemStack(worldIn, pos.getX(), pos.getY(), pos.getZ(), h.getStackInSlot(i));
        }
    }


}
