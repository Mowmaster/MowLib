package com.mowmaster.mowlib.workcard;

import com.mowmaster.mowlib.MowLibIds;
import net.minecraft.network.chat.Component;

// The kinds of work card (1.20's numbers 1, 2, 3), and what a machine accepts.
public enum WorkCardType
{
    // Two corners of a box
    AREA,
    // A list of single positions
    LOCATIONS,
    // A list of machines (Pedestals' pedestals card: crafter, modification upgrade)
    MACHINES,
    // A machine that works with either an area or locations (1.20's type 0)
    AREA_OR_LOCATIONS;

    // Can a machine wanting this type use a card of cardType?
    public boolean accepts(WorkCardType cardType)
    {
        return this == cardType || (this == AREA_OR_LOCATIONS && (cardType == AREA || cardType == LOCATIONS));
    }

    // "Area", "Locations"... (lang key mowlib.workcard_type.<name>)
    public Component getName()
    {
        return Component.translatable(MowLibIds.MODID + ".workcard_type." + name().toLowerCase());
    }
}
