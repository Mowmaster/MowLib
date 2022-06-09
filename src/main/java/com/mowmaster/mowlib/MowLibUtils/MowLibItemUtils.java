package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.Random;

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
}
