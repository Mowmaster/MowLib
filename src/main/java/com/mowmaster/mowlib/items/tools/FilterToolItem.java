package com.mowmaster.mowlib.items.tools;

import net.minecraft.world.item.Item;

// Hold in the offhand and left-click a filter holder (a pedestal...) to take its filter out (1.20's MowLib FilterTool).
// The holder's block does the work (StorageInteractions#removeFilter); this item only needs to exist.
public class FilterToolItem extends MowLibToolItem
{
    public FilterToolItem(Properties properties)
    {
        super(properties);
    }
}
