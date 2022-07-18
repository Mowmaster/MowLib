package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.Capabilities.Experience.IExperienceStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.level.Level;

public class MowLibXpUtils
{
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
