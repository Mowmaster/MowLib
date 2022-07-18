package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.Random;

public class MowLibEnergyUtils {
    public static void removeEnergy(Level worldIn, BlockPos pos, IEnergyStorage energy) {
        Random rand = new Random();
        if(energy.getEnergyStored()>=5000)
        {
            while(energy.getEnergyStored()>=5000)
            {
                LightningBolt lightningbolt = EntityType.LIGHTNING_BOLT.create(worldIn);
                lightningbolt.moveTo(Vec3.atBottomCenterOf(pos.offset(rand.nextInt(10),-1,rand.nextInt(10))));
                lightningbolt.setCause(null);
                worldIn.addFreshEntity(lightningbolt);
                worldIn.playSound(null, pos, SoundEvents.TRIDENT_THUNDER, SoundSource.WEATHER, 5.0F, 1.0F);
                energy.extractEnergy(5000,false);
            }
        }
    }
}
