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
        // save(output, String) reads the string as a full id: "mowlib:picked_dye" -> data/mowlib/recipe/picked_dye.json.
        // (A string without a colon, like the old "mowlib.picked_dye", lands in the minecraft namespace.)
        // Colour picker holding a colour -> custom dye (the picker is returned reset)
        SpecialRecipeBuilder.special(() -> CBPickedDyeRecipe.INSTANCE)
                .save(output, MowLibReferences.id("picked_dye").toString());
        // Any #mowlib:dyeable_in_crafting item + dyes -> the item with the dyes mixed into its colour
        SpecialRecipeBuilder.special(() -> ColorableItemDyeRecipe.INSTANCE)
                .save(output, MowLibReferences.id("colorable_dye").toString());
    }
}
