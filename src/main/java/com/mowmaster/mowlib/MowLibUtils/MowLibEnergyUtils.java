package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import java.util.List;
import java.util.Random;

public class MowLibEnergyUtils {
    public static LazyOptional<IEnergyStorage> findEnergyHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IEnergyStorage> cap = neighbourTile.getCapability(CapabilityEnergy.ENERGY, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IEnergyStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IEnergyStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                    if(cap.isPresent())
                        return cap;
                }
            }
            else
            {
                //Added for quark boats with inventories (i hope)
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof Boat);
                if(!list.isEmpty())
                {
                    LazyOptional<IEnergyStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityEnergy.ENERGY);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

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
