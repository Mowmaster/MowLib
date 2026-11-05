package com.mowmaster.mowlib.items;

import com.mowmaster.mowlib.client.MowLibKeyMappings;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

// A blank that turns into one of a picker group's items (picker/PickerGroup): the Blank Filter, the Blank Work Card,
// or another mod's. Its only job here is the tooltip, item.<namespace>.<id>.tooltip with the activation key's name
// as %s ("Press V while holding it to pick a filter"). Any item can be a group's blank; this one just says how.
public class PickerBaseItem extends Item
{
    public PickerBaseItem(Properties properties)
    {
        super(properties);
    }

    // Component.keybind shows whatever key the player has bound. Only the key's name (a String constant) is used, so
    // this common class never loads the client-only KeyMapping.
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        tooltip.accept(Component.translatable(getDescriptionId() + ".tooltip", Component.keybind(MowLibKeyMappings.ACTIVATE_NAME))
                .withStyle(ChatFormatting.GRAY));
    }
}
