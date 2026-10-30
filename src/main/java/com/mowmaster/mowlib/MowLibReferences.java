package com.mowmaster.mowlib;

import net.minecraft.resources.Identifier;

public class MowLibReferences
{
    public static final String MODID = "mowlib";
    public static final String MODNAME = "MowLib";

    private MowLibReferences()
    {
        
    }

    public static Identifier id(String path)
    {
        return Identifier.fromNamespaceAndPath(MODID, path);
    }
}
