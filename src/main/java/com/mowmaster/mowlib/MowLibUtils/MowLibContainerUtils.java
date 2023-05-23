package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.fluids.FluidUtil;

import java.util.ArrayList;
import java.util.List;

public class MowLibContainerUtils {

    public static Container getContainer(int size)
    {
        return new Container() {
        List<ItemStack> stack = new ArrayList<>();

        @Override
        public int getContainerSize() {
            return size;
        }

        @Override
        public boolean isEmpty() {
            return stack.isEmpty();
        }

        @Override
        public ItemStack getItem(int p_18941_) {
            return stack.get(p_18941_);
        }

        @Override
        public ItemStack removeItem(int p_18942_, int p_18943_) {
            ItemStack oldStack = stack.get(p_18942_).copy();
            stack.remove(p_18942_);
            return oldStack;
        }

        @Override
        public ItemStack removeItemNoUpdate(int p_18951_) {
            ItemStack oldStack = stack.get(p_18951_).copy();
            stack.remove(p_18951_);
            return oldStack;
        }

        @Override
        public void setItem(int p_18944_, ItemStack p_18945_) {
            if(p_18944_ == -1)stack.add(p_18945_);
            else stack.set(p_18944_,p_18945_);
        }

        @Override
        public void setChanged() {

        }

        @Override
        public boolean stillValid(Player p_18946_) {
            return stack.isEmpty();
        }

        @Override
        public void clearContent() {
            stack = new ArrayList<>();
        }
        };
    }

    public static MowLibMultiContainer getMultiContainer(int size)
    {
        return new MowLibMultiContainer() {
            List<ItemStack> stack = new ArrayList<>();
            FluidStack fluidStack = FluidStack.EMPTY;
            int energy = 0;
            int experience = 0;
            DustMagic magic = DustMagic.EMPTY;

            @Override
            public int getContainerSize() {
                return size;
            }

            @Override
            public boolean isEmpty() {
                return (stack.isEmpty() &&
                        fluidStack.isEmpty() &&
                        energy == 0 &&
                        experience == 0 &&
                        magic.isEmpty());

            }

            @Override
            public ItemStack getItem(int p_18941_) {
                return stack.get(p_18941_);
            }

            public FluidStack getFluidStack(){return fluidStack;}

            public int getEnergy(){return energy;}

            public int getExperience(){return experience;}

            public DustMagic getDustMagic(){return magic;}

            @Override
            public ItemStack removeItem(int p_18942_, int p_18943_) {
                ItemStack oldStack = stack.get(p_18942_).copy();
                stack.remove(p_18942_);
                return oldStack;
            }

            @Override
            public ItemStack removeItemNoUpdate(int p_18951_) {
                ItemStack oldStack = stack.get(p_18951_).copy();
                stack.remove(p_18951_);
                return oldStack;
            }

            @Override
            public void setItem(int p_18944_, ItemStack p_18945_) {
                if(p_18944_ == -1)stack.add(p_18945_);
                else stack.set(p_18944_,p_18945_);
            }

            public void setFluidStack(FluidStack fluidIn)
            {
                if(!fluidStack.isEmpty() && FluidStack.areFluidStackTagsEqual(fluidIn,fluidStack)){
                    fluidStack.setAmount(fluidStack.getAmount()+fluidIn.getAmount());
                }
                else
                {
                    fluidStack = new FluidStack(fluidIn.getFluid(), fluidIn.getAmount(), fluidIn.getTag());
                }
            }

            public FluidStack removeFluid()
            {
                FluidStack oldStack = fluidStack;
                fluidStack = FluidStack.EMPTY;
                return oldStack;
            }

            public void setEnergy(int amount)
            {
                if(energy > 0)energy = energy+amount;
                else energy = amount;
            }

            public int removeEnergy()
            {
                int oldEnergy = energy;
                energy = 0;
                return oldEnergy;
            }

            public void setExperience(int amount)
            {
                if(experience > 0)experience = experience+amount;
                else experience = amount;
            }

            public int removeExperience()
            {
                int oldExperience = experience;
                experience = 0;
                return oldExperience;
            }

            public void setDustMagic(DustMagic dustMagic)
            {
                if(magic.isDustEqual(dustMagic))magic.setDustAmount(magic.getDustAmount() + dustMagic.getDustAmount());
                else magic = dustMagic;
            }

            public DustMagic removeDustMagic()
            {
                DustMagic oldStack = magic;
                magic = DustMagic.EMPTY;
                return oldStack;
            }

            @Override
            public void setChanged() {

            }

            @Override
            public boolean stillValid(Player p_18946_) {
                return stack.isEmpty();
            }

            @Override
            public void clearContent() {
                stack = new ArrayList<>();
                fluidStack = FluidStack.EMPTY;
                energy = 0;
                experience = 0;
                magic = DustMagic.EMPTY;
            }
        };
    }

    public static AbstractContainerMenu getAbstractContainerMenu(int id)
    {
        AbstractContainerMenu abstractContainerMenu = new AbstractContainerMenu(null,id)
        {
            @Override
            public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
                return null;
            }

            @Override
            public boolean stillValid(Player p_38874_) {
                return true;
            }
        };

        return abstractContainerMenu;
    }



    public static CraftingContainer getContainerCrafting(int sizeX, int sizeY)
    {
        return new CraftingContainer(getAbstractContainerMenu(40),sizeX,sizeY) {
            List<ItemStack> stack = new ArrayList<>();

            @Override
            public int getContainerSize() {
                return sizeX*sizeY;
            }

            @Override
            public boolean isEmpty() {
                return stack.isEmpty();
            }

            @Override
            public ItemStack getItem(int p_18941_) {
                if(p_18941_ >= getContainerSize())return ItemStack.EMPTY;
                return stack.get(p_18941_);
            }

            @Override
            public ItemStack removeItem(int p_18942_, int p_18943_) {
                if(p_18942_ >= getContainerSize())return ItemStack.EMPTY;
                ItemStack oldStack = stack.get(p_18942_).copy();
                stack.remove(p_18942_);
                return oldStack;
            }

            @Override
            public ItemStack removeItemNoUpdate(int p_18951_) {
                if(p_18951_ >= getContainerSize())return ItemStack.EMPTY;
                ItemStack oldStack = stack.get(p_18951_).copy();
                stack.remove(p_18951_);
                return oldStack;
            }

            @Override
            public void setItem(int p_18944_, ItemStack p_18945_) {
                if(p_18944_ >= getContainerSize())return;
                if(p_18944_ == -1)stack.add(p_18945_);
                else stack.set(p_18944_,p_18945_);
            }

            @Override
            public void setChanged() {

            }

            @Override
            public boolean stillValid(Player p_18946_) {
                return stack.isEmpty();
            }

            @Override
            public void clearContent() {
                stack = new ArrayList<>();
            }
        };
    }
}
