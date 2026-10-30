package com.mowmaster.mowlib.creative;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.registry.MowLibItems;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

public class CreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MowLibReferences.MODID);

    // Every book in each size its recipes make, then the crafting materials.
    // Books: (8 pages plain; copper 12, iron 16, gold 24, as in 1.20)
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOWLIB_TAB = TABS.register("mowlib", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.mowlib"))
            .icon(() -> new ItemStack(MowLibItems.WORKCARD_AREA.get()))
            .displayItems((parameters, output) -> {
                output.accept(MowLibItems.COLOR_PICKER);
                output.accept(MowLibItems.CUSTOM_DYE);
                output.accept(MowLibItems.WORKCARD_AREA);
                output.accept(MowLibItems.WORKCARD_LOCATIONS);
                output.accept(MowLibItems.TOOL_FILTER);
                output.accept(MowLibItems.TOOL_WORK);
                // var: the compiler works out the type (DeferredItem<? extends Item>) from the list
                for (var item : MowLibItems.FILTERS) output.accept(item.get());
            }).build());

    public static void register(IEventBus eventBus)
    {
        TABS.register(eventBus);
    }
}
