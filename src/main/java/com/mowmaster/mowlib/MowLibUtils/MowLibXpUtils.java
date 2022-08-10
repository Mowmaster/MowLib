package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.Capabilities.Experience.CapabilityExperience;
import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.ContainerEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.util.LazyOptional;

import java.util.List;

public class MowLibXpUtils
{
    public static LazyOptional<IExperienceStorage> findExperienceHandlerAtPos(Level world, BlockPos pos, Direction side, boolean allowCart)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IExperienceStorage> cap = neighbourTile.getCapability(CapabilityExperience.EXPERIENCE, side);
            if(cap.isPresent())
                return cap;
        }
        if(allowCart)
        {
            List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof ContainerEntity);
            if(!list.isEmpty())
            {
                LazyOptional<IExperienceStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityExperience.EXPERIENCE);
                if(cap.isPresent())
                    return cap;
            }
            /*if(RailBlock.isRail(world, pos))
            {
                List<Entity> list = world.getEntitiesOfClass(Entity.class, new AABB(pos), entity -> entity instanceof IForgeAbstractMinecart);
                if(!list.isEmpty())
                {
                    LazyOptional<IExperienceStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityExperience.EXPERIENCE);
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
                    LazyOptional<IExperienceStorage> cap = list.get(world.random.nextInt(list.size())).getCapability(CapabilityExperience.EXPERIENCE);
                    if(cap.isPresent())
                        return cap;
                }
            }*/
        }
        return LazyOptional.empty();
    }

    public static int removeXp(Player player, int amount) {
        //Someday consider using player.addExpierence()
        int startAmount = amount;
        while(amount > 0) {
            int barCap = player.getXpNeededForNextLevel();
            int barXp = (int) (barCap * player.experienceProgress);
            int removeXp = Math.min(barXp, amount);
            int newBarXp = barXp - removeXp;
            amount -= removeXp;//amount = amount-removeXp

            player.totalExperience -= removeXp;
            if(player.totalExperience < 0) {
                player.totalExperience = 0;
            }
            if(newBarXp == 0 && amount > 0) {
                player.experienceLevel--;
                if(player.experienceLevel < 0) {
                    player.experienceLevel = 0;
                    player.totalExperience = 0;
                    player.experienceProgress = 0;
                    break;
                } else {
                    player.experienceProgress = 1.0F;
                }
            } else {
                player.experienceProgress = newBarXp / (float) barCap;
            }
        }
        return startAmount - amount;
    }

    public static int getExpCountByLevel(int level)
    {
        int expUsed = 0;

        if(level <= 16)
        {
            expUsed = (level*level) + (6 * level);
        }
        else if(level > 16 && level <=31)
        {
            expUsed = (int)(((2.5 * (level*level)) - (40.5 * level))+360);
        }
        else if(level > 31)
        {
            expUsed = (int)(((4.5 * (level*level)) - (162.5 * level))+2220);
        }

        return expUsed;
    }

    public static int getExpLevelFromCount(int value)
    {
        int level = 0;
        long maths = 0;
        int i = 0;
        int j = 0;

        if(value > 0 && value <= 352)
        {
            maths = (long)Math.sqrt(Math.addExact((long)36, Math.addExact((long)4,(long)value )));
            i = (int)(Math.round(Math.addExact((long)-6 , maths) / 2));
        }
        if(value > 352 && value <= 1507)
        {
            maths = (long)Math.sqrt(Math.subtractExact((long)164025, Math.multiplyExact((long)100,Math.subtractExact((long)3600,Math.multiplyExact((long)10,(long)value)))));

            i = (int)(Math.addExact((long)405 , maths) / 50);
        }
        if(value > 1507)
        {

            maths = (long)Math.sqrt(Math.subtractExact((long)2640625,Math.multiplyExact((long)180, Math.subtractExact((long)22200,Math.multiplyExact((long)10,(long)value)))));
            i = (int)(Math.addExact((long)1625 , maths) / 90);
        }

        return Math.abs(i);
    }

    public static void dropXPInWorld(Level level, BlockPos pos, IExperienceStorage xpStorage) {
        if(xpStorage.getExperienceStored()>0)
        {
            ExperienceOrb xpEntity = new ExperienceOrb(level,pos.getX(), pos.getY(), pos.getZ(),xpStorage.getExperienceStored());
            xpEntity.lerpMotion(0D,0D,0D);
            level.addFreshEntity(xpEntity);
        }
    }
}
