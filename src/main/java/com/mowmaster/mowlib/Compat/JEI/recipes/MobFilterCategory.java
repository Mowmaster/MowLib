package com.mowmaster.mowlib.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Recipes.BaseBlockEntityFilter;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
    private final ItemStack renderStack = new ItemStack(Items.SKELETON_SKULL).setHoverName(Component.translatable(MODID + ".jei.mob_filter_icon"));

    public MobFilterCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(MODID, "textures/gui/jei/effect_crafting.png"), 0, 0, 64, 64);
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
        MutableComponent mobName = (mobType ==0)?(Component.literal(MobCategory.byName(recipe.getEntityString()).getName())):(EntityType.byString(recipe.getEntityString()).get().getDescription().copy());
        if(mobType ==0) mobName = Component.translatable(MODID + ".mob_filter_type").append(mobName);
        if(mobType ==1) mobName = Component.translatable(MODID + ".mob_filter_entity").append(mobName);
        if(recipe.getResultBaby()) mobName.append(Component.translatable(MODID + ".mob_filter_baby"));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 43, 24)
                .addItemStack(new ItemStack(Items.SKELETON_SKULL).setHoverName(mobName));


    }

    @Override
    public void draw(BaseBlockEntityFilter recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        //this.overlay.draw(stack, 48, 0);
    }
}
