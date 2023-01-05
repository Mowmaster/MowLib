package com.mowmaster.mowlib.EventHandlers;

import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

//https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/80a9cbade143008baaa00635d7ac315b192d583c/src/main/java/appeng/core/AppEngBase.java#L116
//https://github.com/AppliedEnergistics/Applied-Energistics-2/blob/80a9cbade143008baaa00635d7ac315b192d583c/src/main/java/appeng/core/MainCreativeTab.java#L41
@Mod.EventBusSubscriber
public class CreativeTabEvent {

    /*@SubscribeEvent
    public void buildContents(CreativeModeTabEvent.Register event) {
        event.registerCreativeModeTab(new ResourceLocation(MowLibReferences.MODID, "mowlib_tab"), builder ->
                // Set name of tab to display
                builder.title(Component.translatable("item_group." + MowLibReferences.MODID + ".mowlib_tab"))
                        // Set icon of creative tab
                        .icon(() -> new ItemStack(DeferredRegisterItems.COLOR_APPLICATOR.get()))
                        // Add default items to tab
                        .displayItems((enabledFlags, populator, hasPermissions) -> {
                            populator.accept(DeferredRegisterItems.COLOR_APPLICATOR.get());
                            populator.accept(DeferredRegisterItems.SCROLL_T2_REPAIR.get());
                            //populator.accept(BLOCK.get());
                        })
        );
    }*/

    // Registered on the MOD event bus
// Assume we have RegistryObject<Item> and RegistryObject<Block> called ITEM and BLOCK
    @SubscribeEvent
    public void buildContents(CreativeModeTabEvent.BuildContents event) {
        // Add to ingredients tab

        if (event.getTab() == CreativeModeTabs.TOOLS_AND_UTILITIES) {
            event.accept(DeferredRegisterItems.COLOR_APPLICATOR.get());
            event.accept(DeferredRegisterItems.SCROLL_T2_REPAIR.get());

            // Takes in an ItemLike, assumes block has registered item
            //event.accept(BLOCK);
        }

    }
}
