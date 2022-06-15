package com.mowmaster.mowlib.api;

import net.minecraft.world.level.block.state.properties.IntegerProperty;

public interface IColorable
{
    IntegerProperty COLOR_RED = IntegerProperty.create("color_red", 0, 3);
    IntegerProperty COLOR_GREEN = IntegerProperty.create("color_green", 0, 3);
    IntegerProperty COLOR_BLUE = IntegerProperty.create("color_blue", 0, 3);
}
