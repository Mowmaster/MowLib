package com.mowmaster.mowlib.items.tools;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

// A tool that only does something from the offhand while left-clicking a block that checks for it (the filter
// tool, the work card tool, Pedestals' upgrade tool...). Shows how to use it in its tooltip.
// Used in a crafting recipe (the work card recipes...) it comes back out of the grid instead of being used up.
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

    // What's left in the grid after crafting: the tool itself, unchanged (any data on it kept). NeoForge's version of
    // this takes the actual stack (an ItemInstance); since 26.1 the result is an ItemStackTemplate (an immutable
    // item + count + data). The same pattern as CraftingToolItem, without the wear.
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance)
    {
        if (instance instanceof ItemStack stack && !stack.isEmpty()) return ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1));
        return new ItemStackTemplate(this, 1);
    }
}
