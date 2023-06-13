package com.mowmaster.mowlib.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Recipes.InWorldDualHandedCrafting;
import com.mowmaster.mowlib.Recipes.MobEffectColorRecipeCorrupted;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class DualHandedCraftingRecipeCategory implements IRecipeCategory<InWorldDualHandedCrafting>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.ICON_HAND.get());

    public DualHandedCraftingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(MODID, "textures/gui/jei/dual_handed_crafting.png"), 0, 0, 196, 128);
        this.localizedName = Component.translatable(MODID + ".jei.dual_handed_crafting");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<InWorldDualHandedCrafting> getRecipeType() {
        return JEIRecipeTypes.DUAL_HANDED_CRAFING;
    }

    @Override
    public Component getTitle() {
        return this.localizedName;
    }

    @Override
    public IDrawable getBackground() {
        return this.background;
    }

    @Override
    public IDrawable getIcon() {
        return this.icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, InWorldDualHandedCrafting recipe, IFocusGroup focuses) {
        //Offhand
        builder.addSlot(RecipeIngredientRole.INPUT, 33, 33)
                .addIngredients(recipe.getIngredients().get(2));
        //Mainhand
        builder.addSlot(RecipeIngredientRole.INPUT, 144, 33)
                .addIngredients(recipe.getIngredients().get(1));
        //Block Clicked On
        builder.addSlot(RecipeIngredientRole.INPUT, 90, 63)
                .addIngredients(recipe.getIngredients().get(0));
        //Result
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 95)
                .addItemStack(recipe.getResultItemJEI());
    }


    @Override
    public void draw(InWorldDualHandedCrafting recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
    }
}
