package com.mowmaster.mowlib.filter;

import com.mowmaster.mowlib.MowLibIds;
import net.minecraft.network.chat.Component;

// When a filter applies: to what goes INTO the holder, what's taken OUT of it, or neither (NEUTRAL, used by
// machines that read the filter themselves, e.g. block breakers). Same values as 1.20's MowLib FilterDirection.
public enum FilterDirection
{
    INSERT,
    EXTRACT,
    NEUTRAL;

    public Component getName()
    {
        return Component.translatable(MowLibIds.MODID + ".filter_direction." + name().toLowerCase());
    }
}
