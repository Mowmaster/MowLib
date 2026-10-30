package com.mowmaster.mowlib.registry;

import com.mojang.serialization.MapCodec;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.recipes.CBPickedDyeRecipe;
import com.mowmaster.mowlib.recipes.ColorableItemDyeRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class MowLibRecipes
{
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, MowLibReferences.MODID);

    // The recipe has no settings, so both codecs just produce the single instance
    public static final Supplier<RecipeSerializer<CBPickedDyeRecipe>> PICKED_DYE = RECIPE_SERIALIZERS.register("picked_dye",
            () -> new RecipeSerializer<>(MapCodec.unit(CBPickedDyeRecipe.INSTANCE), StreamCodec.unit(CBPickedDyeRecipe.INSTANCE)));

    public static final Supplier<RecipeSerializer<ColorableItemDyeRecipe>> COLORABLE_DYE = RECIPE_SERIALIZERS.register("colorable_dye",
            () -> new RecipeSerializer<>(MapCodec.unit(ColorableItemDyeRecipe.INSTANCE), StreamCodec.unit(ColorableItemDyeRecipe.INSTANCE)));

    public static void register(IEventBus eventBus)
    {
        RECIPE_SERIALIZERS.register(eventBus);
    }
}
