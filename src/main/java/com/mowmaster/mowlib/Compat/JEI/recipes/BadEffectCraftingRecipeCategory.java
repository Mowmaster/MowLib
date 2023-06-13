package com.mowmaster.mowlib.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.Items.EffectItemBase;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
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
import net.minecraft.core.Registry;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Random;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class BadEffectCraftingRecipeCategory implements IRecipeCategory<MobEffectColorRecipeCorrupted>
{
    private final IDrawable background;
    private final Component localizedName;
    //private final IDrawable overlay;
    private final IDrawable icon;
    private final ItemStack renderStack = new ItemStack(DeferredRegisterItems.ICON_DUST.get());

    public BadEffectCraftingRecipeCategory(IGuiHelper guiHelper) {
        this.background = guiHelper.createDrawable(
                new ResourceLocation(MODID, "textures/gui/jei/effect_crafting.png"), 0, 0, 64, 64);
        this.localizedName = Component.translatable(MODID + ".jei.bad_effect_crafting");
        //this.overlay =
        this.icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, this.renderStack);
        //this.renderStack.getOrCreateTag().putBoolean("RenderFull", true);
    }

    @Override
    public RecipeType<MobEffectColorRecipeCorrupted> getRecipeType() {
        return JEIRecipeTypes.BADEFFECT_CRAFING;
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
    public void setRecipe(IRecipeLayoutBuilder builder, MobEffectColorRecipeCorrupted recipe, IFocusGroup focuses) {
        //Way to link the crystals to the right recipe
        builder.addSlot(RecipeIngredientRole.CATALYST, 24,5)
                .addIngredients(recipe.getIngredients().get(0));

        //Input Color
        builder.addSlot(RecipeIngredientRole.INPUT, 5, 24)
                .addItemStack(MowLibColorReference.addColorToItemStack(new ItemStack(DeferredRegisterItems.ICON_DUST.get()),recipe.getResultEffectColor()).setHoverName(Component.translatable(MODID + MowLibColorReference.getColorName(recipe.getResultEffectColor()))));

        //Result
        ItemStack returner = MowLibColorReference.addColorToItemStack(new ItemStack(DeferredRegisterItems.ICON_EFFECT.get()),recipe.getResultEffectColor());

        MobEffectInstance effect = (recipe.getResultEffectName() == "")?(new MobEffectInstance(getRandomNegativeEffect())):(new MobEffectInstance(ForgeRegistries.MOB_EFFECTS.getValue(new ResourceLocation(recipe.getResultEffectName()))));
        EffectItemBase.setEffectToItem(returner,effect);
        builder.addSlot(RecipeIngredientRole.OUTPUT, 43, 24)
                .addItemStack(returner);
    }

    @Override
    public void draw(MobEffectColorRecipeCorrupted recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics guiGraphics, double mouseX, double mouseY) {
        RenderSystem.enableBlend();
    }

}
