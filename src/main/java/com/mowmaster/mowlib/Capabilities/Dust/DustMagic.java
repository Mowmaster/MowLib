package com.mowmaster.mowlib.Capabilities.Dust;

import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.fluids.FluidStack;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class DustMagic {

    //Should Probably consider making a registry for this
    //https://github.com/hjake123/reactive/blob/main/src/main/java/com/hyperlynx/reactive/alchemy/Power.java
    //https://github.com/hjake123/reactive/blob/main/src/main/java/com/hyperlynx/reactive/alchemy/Powers.java
    //

    private int dustColor;
    private int dustAmount;

    public DustMagic(int color, int amount)
    {
        this.dustColor = color;
        this.dustAmount = amount;
    }

    public static final DustMagic EMPTY = new DustMagic(-1, 0);

    public boolean isEmpty() { return this.getDustColor() == -1; }

    public DustMagic copy() { return new DustMagic(this.dustColor,this.dustAmount); }

    public int getDustColor() {
        return this.dustColor;
    }

    public int getDustAmount() {
        return this.dustAmount;
    }

    public void setDustColor(int dustColor) {
        this.dustColor = dustColor;
    }

    public void setDustAmount(int dustAmount) {
        this.dustAmount = dustAmount;
    }

    public void grow(int amount) { setDustAmount(this.dustAmount + amount); }

    public void shrink(int amount) {
        setDustAmount(this.dustAmount - amount);
    }

    @Override
    public final boolean equals(Object o)
    {
        if (!(o instanceof DustMagic))
        {
            return false;
        }
        return isDustEqual((DustMagic) o);
    }

    public boolean isDustEqual(DustMagic magicIn)
    {
        return magicIn.getDustColor() == this.dustColor;
    }

    public boolean isDustEqualOrEmpty(DustMagic magicIn)
    {
        if(this.equals(new DustMagic(-1, 0)) || this.dustAmount <=0)return true;
        else if(this.dustAmount > 0)return magicIn.getDustColor() == this.dustColor;
        return false;
    }

    public static CompoundTag setDustMagicInTag(CompoundTag tag, DustMagic magicIn)
    {
        tag.putInt(MODID + "_dustMagicColor",magicIn.getDustColor());
        tag.putInt(MODID + "_dustMagicAmount",magicIn.getDustAmount());

        return tag;
    }

    public static ItemStack setDustMagicInStack(ItemStack stackToSet, DustMagic magicIn)
    {
        ItemStack copyStack = stackToSet.copy();
        CompoundTag copyStackTag = stackToSet.getOrCreateTag();
        copyStackTag.putInt(MODID + "_dustMagicColor",magicIn.getDustColor());
        int dustAmount = magicIn.getDustAmount();
        if(stackToSet.getCount() > 1)
        {
            double maths = magicIn.getDustAmount()/stackToSet.getCount();
            dustAmount = (int)Math.floor(maths);
        }
        copyStackTag.putInt(MODID + "_dustMagicAmount",dustAmount);
        copyStack.setTag(copyStackTag);
        return copyStack;
    }

    public static DustMagic getDustMagicInTag(CompoundTag tag)
    {
        DustMagic magic = new DustMagic(-1, 0);

        if(tag.contains(MODID + "_dustMagicColor"))
        {
            magic.setDustColor(tag.getInt(MODID + "_dustMagicColor"));
            if(tag.contains(MODID + "_dustMagicAmount"))
            {
                magic.setDustAmount(tag.getInt(MODID + "_dustMagicAmount"));
            }
        }

        return magic;
    }

    public static DustMagic getDustMagicInItemStack(ItemStack stack)
    {
        DustMagic magic = new DustMagic(-1, 0);
        if(stack.hasTag())
        {
            CompoundTag tag = stack.getTag();
            if(tag.contains(MODID + "_dustMagicColor"))
            {
                magic.setDustColor(tag.getInt(MODID + "_dustMagicColor"));
                if(tag.contains(MODID + "_dustMagicAmount"))
                {
                    magic.setDustAmount(tag.getInt(MODID + "_dustMagicAmount")*stack.getCount());
                }
            }
            else if(tag.contains(MODID+"_color"))
            {
                magic.setDustColor(MowLibColorReference.getColorFromItemStackInt(stack));
                magic.setDustAmount(stack.getCount());
            }
        }
        return magic;
    }

}
