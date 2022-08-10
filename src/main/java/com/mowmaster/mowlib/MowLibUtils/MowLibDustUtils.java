package com.mowmaster.mowlib.MowLibUtils;

import com.mowmaster.mowlib.Capabilities.Dust.CapabilityDust;
import com.mowmaster.mowlib.Capabilities.Dust.IDustHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.common.util.LazyOptional;

public class MowLibDustUtils
{
    public static LazyOptional<IDustHandler> findDustHandlerAtPos(Level world, BlockPos pos, Direction side)
    {
        BlockEntity neighbourTile = world.getBlockEntity(pos);
        if(neighbourTile!=null)
        {
            LazyOptional<IDustHandler> cap = neighbourTile.getCapability(CapabilityDust.DUST_HANDLER, side);
            if(cap.isPresent())
                return cap;
        }

        return LazyOptional.empty();
    }
}
