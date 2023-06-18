package com.mowmaster.mowlib.MowLibUtils;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Recipes.Ingredients.FluidTagIngredient;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.AbstractCookingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import net.minecraft.world.level.material.Fluid;
import net.minecraftforge.fluids.FluidStack;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;

public class MowLibRecipeUtils {
    public static void resetCachedAbstractCooking(String modID, ItemStack stackToStoreNBT) {
        CompoundTag tag = stackToStoreNBT.getOrCreateTag();
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_ingredient");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_result");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_time");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_xp_gain");
        MowLibCompoundTagUtils.removeCustomTagFromNBT(modID, tag, "cook_cached");
    }

    private static boolean hasCachedAbstractCooking(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readBooleanFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_cached");
    }

    // NOTE: This SHOULD NOT be called if `hasCachedAbstractCooking` returns `false`.
    private static boolean cachedAbstractCookingHasSameInput(String modID, ItemStack stackToStoreNBT, ItemStack input) {
        ItemStack cachedInput = MowLibCompoundTagUtils.readItemStackFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_ingredient");
        return ItemHandlerHelper.canItemStacksStack(input, cachedInput) ||
                input.isEmpty() && cachedInput.isEmpty(); // ItemStack.EMPTY doesn't "stack" but we want to consider them a match
    }


    private static <T extends AbstractCookingRecipe> ItemStack getAndCacheAbstractCookingResult(String modID, Level level, ItemStack stackToStoreNBT, ItemStack input, RecipeType<T> recipeType) {
        Container container = new SimpleContainer(input);

        RecipeManager recipeManager = level.getRecipeManager();
        Optional<T> result = recipeManager.getRecipeFor(recipeType, container, level);
        if (result.isPresent()) {
            T recipe = result.get();
            ItemStack resultItem = recipe.getResultItem(level.registryAccess());
            CompoundTag tag = stackToStoreNBT.getOrCreateTag();
            MowLibCompoundTagUtils.writeItemStackToNBT(modID, tag, input, "cook_ingredient");
            MowLibCompoundTagUtils.writeItemStackToNBT(modID, tag, resultItem,"cook_result");
            MowLibCompoundTagUtils.writeIntegerToNBT(modID, tag, recipe.getCookingTime(), "cook_time");
            MowLibCompoundTagUtils.writeIntegerToNBT(modID, tag, Math.round(recipe.getExperience()), "cook_xp_gain");
            MowLibCompoundTagUtils.writeBooleanToNBT(modID, tag, true, "cook_cached");
            return resultItem;
        } else {
            MowLibCompoundTagUtils.writeBooleanToNBT(modID, stackToStoreNBT.getOrCreateTag(), false,"cook_cached");
            return ItemStack.EMPTY;
        }
    }

    public static <T extends AbstractCookingRecipe> ItemStack getAbstractCookingResult(String modID, Level level, ItemStack stackToStoreNBT, ItemStack ingredient, RecipeType<T> recipeType) {
        if (hasCachedAbstractCooking(modID, stackToStoreNBT) && cachedAbstractCookingHasSameInput(modID, stackToStoreNBT, ingredient)) {
            return MowLibCompoundTagUtils.readItemStackFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_result");
        }
        return getAndCacheAbstractCookingResult(modID, level, stackToStoreNBT, ingredient, recipeType);
    }

    // NOTE: This SHOULD NOT be called if `getAbstractCookingResult` returns `ItemStack.EMPTY`.
    public static int getXpGainFromCachedRecipe(String modID, ItemStack stackToStoreNBT)  {
        return MowLibCompoundTagUtils.readIntegerFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_xp_gain");
    }

    // NOTE: This SHOULD NOT be called if `getAbstractCookingResult` returns `ItemStack.EMPTY`.`
    public static int getCookTimeRequired(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_time");
    }

    public static int getCookTimeElapsed(String modID, ItemStack stackToStoreNBT) {
        return MowLibCompoundTagUtils.readIntegerFromNBT(modID, stackToStoreNBT.getOrCreateTag(), "cook_time_elapsed");
    }
    public static void setCookTimeElapsed(String modID, ItemStack stackToStoreNBT, int timeElapsed) {
        MowLibCompoundTagUtils.writeIntegerToNBT(modID, stackToStoreNBT.getOrCreateTag(), timeElapsed, "cook_time_elapsed");
    }

    public static boolean matchFluid(FluidStack tileFluid, FluidTagIngredient ing) {
        if (tileFluid == null || tileFluid.isEmpty()) {
            return false;
        }
        if (ing.hasFluid() && tileFluid.getFluid() == ing.getFluidStack().getFluid()) {
            return true;
        }
        //either recipe has no fluid or didnt match, try for tag
        if (ing.hasTag()) {
            //see /data/<id>/tags/fluids/
            TagKey<Fluid> ft = FluidTags.create(new ResourceLocation(ing.getTag()));
            if ((ft != null && tileFluid.getFluid() != null && tileFluid.getFluid().is(ft))) {
                return true;
            }
        }
        return false;
    }

    public static FluidTagIngredient parseFluid(JsonObject json, String key) {
        JsonObject mix = json.get(key).getAsJsonObject();
        int count = mix.get("count").getAsInt();
        if (count < 1) {
            count = 1;
        }
        FluidStack fluidstack = FluidStack.EMPTY;
        if (mix.has("fluid")) {
            String fluidId = mix.get("fluid").getAsString(); // JSONUtils.getString(mix, "fluid");
            ResourceLocation resourceLocation = new ResourceLocation(fluidId);
            Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
            fluidstack = (fluid == null) ? FluidStack.EMPTY : new FluidStack(fluid, count);
        }
        String ftag = mix.has("tag") ? mix.get("tag").getAsString() : "";
        return new FluidTagIngredient(fluidstack, ftag, count);
    }

    public static NonNullList<Ingredient> getIngredientsArray(JsonObject obj) {
        JsonArray array = GsonHelper.getAsJsonArray(obj, "ingredients");
        NonNullList<Ingredient> nonnulllist = NonNullList.create();
        for (int i = 0; i < array.size(); ++i) {
            Ingredient ingredient = Ingredient.fromJson(array.get(i));
            if (!ingredient.isEmpty()) {
                nonnulllist.add(ingredient);
            }
        }
        return nonnulllist;
    }

    public static FluidStack getFluid(JsonObject fluidJson) {
        if (fluidJson.has("fluidTag")) {
            //      String fluidTag = fluidJson.get("fluidTag").getAsString();
        }
        String fluidId = GsonHelper.getAsString(fluidJson, "fluid");
        ResourceLocation resourceLocation = new ResourceLocation(fluidId);
        Fluid fluid = ForgeRegistries.FLUIDS.getValue(resourceLocation);
        int count = fluidJson.get("count").getAsInt();
        if (count < 1) {
            count = 1;
        }
        return new FluidStack(fluid, count);
    }
}
