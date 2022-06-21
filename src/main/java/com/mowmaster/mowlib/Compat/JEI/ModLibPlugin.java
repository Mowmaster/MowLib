package com.mowmaster.mowlib.Compat.JEI;

import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class ModLibPlugin implements IModPlugin
{
    public static final ResourceLocation PLUGIN_UID = new ResourceLocation(MODID, "plugin/main");

    @Override
    public ResourceLocation getPluginUid() {
        return PLUGIN_UID;
    }

    private static void addInfoPage(IRecipeRegistration reg, Collection<Item> items, String name) {
        if (items.isEmpty()) return;
        String key = getDescKey(new ResourceLocation(MODID, name));
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        reg.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, Component.translatable(I18n.get(key)));
    }

    public static void addValueInfoPage(IRecipeRegistration reg, Item item, String name, Object... values) {
        Collection<Item> items = Collections.singletonList(item);
        addValueInfoPage(reg, items, name, values);
    }

    private static void addValueInfoPage(IRecipeRegistration reg, Collection<Item> items, String name, Object... values) {
        if (items.isEmpty()) return;
        String key = getDescKey(new ResourceLocation(MODID, name));
        List<ItemStack> stacks = items.stream().map(ItemStack::new).collect(Collectors.toList());
        reg.addIngredientInfo(stacks, VanillaTypes.ITEM_STACK, Component.translatable(I18n.get(key, values)));
    }

    private static String getDescKey(ResourceLocation name) {
        return  name.getPath();
    }

    private static Item getItemFromIngredient(Ingredient ingredient) {
        return ingredient.getItems()[0].getItem();
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration)
    {
        addValueInfoPage(registration, DeferredRegisterItems.COLOR_APPLICATOR.get(),DeferredRegisterItems.COLOR_APPLICATOR.get().getDescriptionId()+".description");
        //addValueInfoPage(registration, DeferredRegisterItems.COLOR_APPLICATOR.get(),DeferredRegisterItems.COLOR_APPLICATOR.get().getDescriptionId()+".interaction");
        addValueInfoPage(registration, DeferredRegisterItems.SCROLL_T2_REPAIR.get(),DeferredRegisterItems.SCROLL_T2_REPAIR.get().getDescriptionId()+".description");
        //addValueInfoPage(registration, DeferredRegisterItems.SCROLL_T2_REPAIR.get(),DeferredRegisterItems.SCROLL_T2_REPAIR.get().getDescriptionId()+".interaction");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registry)
    {

    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration)
    {

    }

}
