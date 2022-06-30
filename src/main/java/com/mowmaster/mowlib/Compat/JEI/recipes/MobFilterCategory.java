package com.mowmaster.mowlib.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Items.EffectItemBase;
import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import com.mowmaster.mowlib.Recipes.BaseBlockEntityFilter;
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
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MobFilterCategory implements IRecipeCategory<BaseBlockEntityFilter>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.ICON_HAND.get());

    public MobFilterCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(MODID, "textures/gui/jei/effect.png"), 0, 0, 196, 128);
        this.localizedName = Component.translatable(MODID + ".jei.mob_filter");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<BaseBlockEntityFilter> getRecipeType() {
        return JEIRecipeTypes.MOB_FILTER;
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
    public void setRecipe(IRecipeLayoutBuilder builder, BaseBlockEntityFilter recipe, IFocusGroup focuses) {

        //Input Color
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 24)
                .addIngredients(recipe.getIngredients().get(0));

        //Result
        int mobType = recipe.getResultMobType();
        String mobName = (mobType ==0)?(MobCategory.byName(recipe.getEntityString()).getName()):(EntityType.byString(recipe.getEntityString()).get().toString());
        if(recipe.getResultBaby()) mobName += " Baby";
        builder.addSlot(RecipeIngredientRole.OUTPUT, 43, 24)
                .addItemStack(new ItemStack(Items.SKELETON_SKULL).setHoverName(Component.literal(mobName)));
    }

    @Override
    public void draw(BaseBlockEntityFilter recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        //this.overlay.draw(stack, 48, 0);
    }
}
