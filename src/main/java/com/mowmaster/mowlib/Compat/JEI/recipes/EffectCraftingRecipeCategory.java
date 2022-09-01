package com.mowmaster.mowlib.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Items.EffectItemBase;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import com.mowmaster.mowlib.Recipes.MobEffectColorRecipe;
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
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

import java.util.Map;
import java.util.Random;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class EffectCraftingRecipeCategory implements IRecipeCategory<MobEffectColorRecipe>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.ICON_DUST.get());

    public EffectCraftingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(MODID, "textures/gui/jei/effect_crafting.png"), 0, 0, 64, 64);
        this.localizedName = Component.translatable(MODID + ".jei.effect_crafting");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<MobEffectColorRecipe> getRecipeType() {
        return JEIRecipeTypes.EFFECT_CRAFING;
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

    public MobEffect getRandomNegativeEffect()
    {
        Random rand = new Random();
        Map<Integer, MobEffect> NEGEFFECT = Map.ofEntries(
                Map.entry(0, MobEffects.BAD_OMEN),
                Map.entry(1,MobEffects.BLINDNESS),
                Map.entry(2,MobEffects.GLOWING),
                Map.entry(3,MobEffects.HUNGER),
                Map.entry(4,MobEffects.HARM),
                Map.entry(5,MobEffects.LEVITATION),
                Map.entry(6,MobEffects.DIG_SLOWDOWN),
                Map.entry(7,MobEffects.CONFUSION),
                Map.entry(8,MobEffects.POISON),
                Map.entry(9,MobEffects.MOVEMENT_SLOWDOWN),
                Map.entry(10,MobEffects.UNLUCK),
                Map.entry(11,MobEffects.WEAKNESS),
                Map.entry(12,MobEffects.WITHER)
        );

        return NEGEFFECT.getOrDefault(rand.nextInt(13),MobEffects.HUNGER);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, MobEffectColorRecipe recipe, IFocusGroup focuses) {
        //Way to link the crystals to the right recipe
        builder.addSlot(RecipeIngredientRole.CATALYST, 24,5)
                .addIngredients(recipe.getIngredients().get(0));

        //Input Color
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 24)
                .addItemStack(MowLibColorReference.addColorToItemStack(new ItemStack(DeferredRegisterItems.ICON_DUST.get()),recipe.getResultEffectColor()).setHoverName(Component.translatable(MowLibColorReference.getColorName(recipe.getResultEffectColor()))));

        //Result
        ItemStack returner = MowLibColorReference.addColorToItemStack(new ItemStack(DeferredRegisterItems.ICON_EFFECT.get()),recipe.getResultEffectColor());
        MobEffectInstance effect = (recipe.getResultEffectName() == "")?(new MobEffectInstance(getRandomNegativeEffect())):(new MobEffectInstance(Registry.MOB_EFFECT.getOptional(new ResourceLocation(recipe.getResultEffectName())).get()));
        EffectItemBase.setEffectToItem(returner,effect);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 43, 24)
                .addItemStack(returner);
    }

    @Override
    public void draw(MobEffectColorRecipe recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
        //this.overlay.draw(stack, 48, 0);
    }
}
