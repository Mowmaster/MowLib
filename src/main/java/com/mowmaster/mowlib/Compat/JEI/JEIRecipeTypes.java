package com.mowmaster.mowlib.Compat.JEI;

import com.mowmaster.mowlib.Recipes.*;
import mezz.jei.api.recipe.RecipeType;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class JEIRecipeTypes
{
    public static final RecipeType<InWorldDualHandedCrafting> DUAL_HANDED_CRAFING =
            RecipeType.create(MODID, "dual_handed_crafting", InWorldDualHandedCrafting.class);

    public static final RecipeType<WorkStationBaseTypeRecipe> WORKSTATIONBASE_CRAFING =
            RecipeType.create(MODID, "workstationbase_crafting", WorkStationBaseTypeRecipe.class);

    public static final RecipeType<MachineBaseTypeRecipe> MACHINEBASE_CRAFING =
            RecipeType.create(MODID, "machinebase_crafting", MachineBaseTypeRecipe.class);

    public static final RecipeType<MobEffectColorRecipe> EFFECT_CRAFING =
            RecipeType.create(MODID, "effect_crafting", MobEffectColorRecipe.class);

    public static final RecipeType<MobEffectColorRecipeCorrupted> BADEFFECT_CRAFING =
            RecipeType.create(MODID, "bad_effect_crafting", MobEffectColorRecipeCorrupted.class);

    public static final RecipeType<BaseBlockEntityFilter> MOB_FILTER =
            RecipeType.create(MODID, "mob_filter", BaseBlockEntityFilter.class);






}
