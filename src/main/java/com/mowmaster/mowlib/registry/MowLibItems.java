package com.mowmaster.mowlib.registry;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.items.CBColorPickerItem;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class MowLibItems
{
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(MowLibReferences.MODID);

    // Right-click a block to store its colour; craft it alone for a Custom Dye of that colour
    public static final DeferredItem<Item> COLOR_PICKER = ITEMS.registerItem("color_picker",
            properties -> new CBColorPickerItem(properties.stacksTo(1)));
    // A dye of any colour, stored in its dyed_color component
    public static final DeferredItem<Item> CUSTOM_DYE = ITEMS.registerItem("custom_dye", Item::new);

    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
