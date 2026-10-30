package com.mowmaster.mowlib.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// A tool that only does something from the offhand while left-clicking a block that checks for it (the filter
// tool, the work card tool, Pedestals' upgrade tool...). Shows how to use it in its tooltip.
public class MowLibToolItem extends Item
{
    public MowLibToolItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    // Lang key item.<namespace>.<id>.tooltip
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip").withStyle(ChatFormatting.GRAY));
    }
}
