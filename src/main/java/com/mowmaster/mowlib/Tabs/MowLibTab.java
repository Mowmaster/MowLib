package com.mowmaster.mowlib.Tabs;

import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class MowLibTab extends CreativeModeTab
{
    public MowLibTab() {
        super("tab_mowlibtab");
    }

    public static final MowLibTab TAB_ITEMS = new MowLibTab() {};

    @Override
    public ItemStack makeIcon() {
        return new ItemStack(DeferredRegisterItems.COLOR_APPLICATOR.get());
    }
}
