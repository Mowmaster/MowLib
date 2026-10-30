package com.mowmaster.mowlib.datagen;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.recipes.CBPickedDyeRecipe;
import com.mowmaster.mowlib.recipes.ColorableItemDyeRecipe;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.registries.MultiRegistryBootstrap;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.SpecialRecipeBuilder;
import net.minecraft.data.worldgen.BootstrapContext;
import net.minecraft.world.item.crafting.Recipe;

public class MowLibRecipeProvider extends RecipeProvider
{
    protected MowLibRecipeProvider(BootstrapContext<Recipe<?>> recipeOutput, BootstrapContext<Advancement> advancementOutput)
    {
        super(recipeOutput, advancementOutput);
    }

    public static MultiRegistryBootstrap create()
    {
        return RecipeProvider.asBootstrap(MowLibRecipeProvider::new);
    }

    @Override
    protected void buildRecipes()
    {
        // Colour picker holding a colour -> custom dye (the picker is returned reset)
        SpecialRecipeBuilder.special(() -> CBPickedDyeRecipe.INSTANCE)
                .save(output, MowLibReferences.MODID + ".picked_dye");
        // Any #mowlib:dyeable_in_crafting item + dyes -> the item with the dyes mixed into its colour
        SpecialRecipeBuilder.special(() -> ColorableItemDyeRecipe.INSTANCE)
                .save(output, MowLibReferences.MODID + ".colorable_dye");
    }
}
