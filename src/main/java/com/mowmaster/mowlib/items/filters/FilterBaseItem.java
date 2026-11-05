package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.client.MowLibKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// The Blank Filter (mowlib:filter_base). Holding it, the activation key opens a radial menu of every filter
// (client/FilterPickerScreen, filter/FilterPicker); picking one swaps one Blank Filter for it. Any filter crafted on its
// own turns back into a Blank Filter (recipes/FilterResetRecipe).
public class FilterBaseItem extends Item
{
    public FilterBaseItem(Properties properties)
    {
        super(properties);
    }

    // "Press V while holding it to pick a filter": Component.keybind shows whatever key the player has bound. Only the
    // key's name (a String) is used here, so this common class never touches the client-only KeyMapping.
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip", Component.keybind(MowLibKeyMappings.ACTIVATE_NAME))
                .withStyle(ChatFormatting.GRAY));
    }
}
