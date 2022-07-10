package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.MowLibUtils.NameComponentUtils;
import com.mowmaster.mowlib.MowLibUtils.TooltipUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

import net.minecraft.world.item.Item.Properties;

public class EffectItemBase extends Item
{

    private MobEffectInstance storedPotionEffect;

    public EffectItemBase(Properties p_41383_)
    {
        super(p_41383_);
        this.storedPotionEffect = null;
    }

    public void setEffectToItem(ItemStack stack)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag())tag = stack.getTag();
        if(storedPotionEffect!=null)storedPotionEffect.save(tag);
        stack.setTag(tag);
    }

    public static void setEffectToItem(ItemStack stack, MobEffectInstance instance)
    {
        CompoundTag tag = new CompoundTag();
        if(stack.hasTag())tag = stack.getTag();
        if(instance!=null)instance.save(tag);
        stack.setTag(tag);
    }

    public static MobEffectInstance getEffectFromItem(ItemStack stack)
    {
        if(stack.hasTag())
        {
            MobEffectInstance instance = null;
            CompoundTag tag = stack.getTag();
            return (MobEffectInstance.load(tag)!=null)?(MobEffectInstance.load(tag)):(null);
        }
        return null;
    }

    @Override
    public Component getName(ItemStack p_41458_) {
        MobEffectInstance getEffect = getEffectFromItem(p_41458_);
        if(getEffect != null)
        {
            return NameComponentUtils.createComponentName(getEffect.getEffect().getDisplayName(), Component.translatable(MODID + ".effect_scroll.text").getString());
        }

        return super.getName(p_41458_);
    }
}
