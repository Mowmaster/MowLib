package com.mowmaster.mowlib.Compat.JEI.recipes;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Compat.JEI.JEIRecipeTypes;
import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import com.mowmaster.mowlib.Recipes.InWorldDualHandedCrafting;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
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

        //Mainhand
        builder.addSlot(RecipeIngredientRole.INPUT, 43, 25)
                .addIngredients(recipe.getIngredients().get(1));
        //Offhand
        builder.addSlot(RecipeIngredientRole.INPUT, 43, 57)
                .addIngredients(recipe.getIngredients().get(2));
        //Block Clicked On
        builder.addSlot(RecipeIngredientRole.INPUT, 102, 41)
                .addIngredients(recipe.getIngredients().get(0));
        if(recipe.getResultModificationName() != "")
        {
            //Result
            builder.addSlot(RecipeIngredientRole.RENDER_ONLY, 102, 75)
                    .addItemStack((recipe.modifyMainHand())?(recipe.getIngredients().get(1).getItems()[0].setHoverName(Component.translatable(MODID + ".dual_handed.warning"))):(recipe.getIngredients().get(2).getItems()[0].setHoverName(Component.translatable(MODID + ".dual_handed.warning"))));
        }
        else
        {
            //Result
            builder.addSlot(RecipeIngredientRole.OUTPUT, 102, 75)
                    .addItemStack(recipe.getResultItem());
        }
    }

    @Override
    public void draw(InWorldDualHandedCrafting recipe, IRecipeSlotsView recipeSlotsView, PoseStack stack, double mouseX, double mouseY) {
        if(recipe.getResultModificationName() != "")
        {
            RenderSystem.enableBlend();
            Font fontRenderer = Minecraft.getInstance().font;

            MutableComponent modType = Component.translatable( "dual_handed." + recipe.getResultModificationName());
            modType.withStyle(ChatFormatting.BLACK);
            int width1 = fontRenderer.width(modType.getString());
            fontRenderer.draw(stack,modType,98-Math.floorDiv(width1,2),4,0xffffffff);


            //+2 - Max 10

            //+
            MutableComponent separator1 = Component.translatable(MowLibReferences.MODID + ".text.separator.plus");
            //" "
            MutableComponent spaceText = Component.translatable(MowLibReferences.MODID + ".text.separator.space");
            //"Min: "
            MutableComponent minText = Component.translatable(MowLibReferences.MODID + ".text.separator.min");
            //"Max: "
            MutableComponent maxText = Component.translatable(MowLibReferences.MODID + ".text.separator.max");

            MutableComponent increaseAmount = Component.literal(""+ recipe.getResultModificationAmount() +"");
            MutableComponent minAmount = Component.literal(""+ recipe.getResultModificationMinAmount() +"");
            MutableComponent maxAmount = Component.literal(""+ recipe.getResultModificationMaxAmount() +"");


            separator1.append(increaseAmount);
            separator1.append(spaceText);
            separator1.append(minText);
            separator1.append(minAmount);
            separator1.append(spaceText);
            separator1.append(maxText);
            separator1.append(maxAmount);
            separator1.withStyle(ChatFormatting.BLACK);

            int width = fontRenderer.width(separator1.getString());
            fontRenderer.draw(stack,separator1,98-Math.floorDiv(width,2),100,0xffffffff);
        }
    }
}
