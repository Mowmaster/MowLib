package com.mowmaster.mowlib.Registry;

import com.mowmaster.mowlib.Items.Filters.BaseFilter;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class ItemModelPropertiesMowLib
{
    public static void dustItemModes(Item item)
    {
        ItemProperties.register(item, new ResourceLocation(MODID + ":filter_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return BaseFilter.getFilterModeForRender(p_174625_);});
        /*ItemProperties.register(item, new ResourceLocation(MODID + ":filter_mode"),(p_174625_, p_174626_, p_174627_, p_174628_) -> {
            return BaseFilter.getFilterModeForRender(p_174625_);});*/
    }
}
