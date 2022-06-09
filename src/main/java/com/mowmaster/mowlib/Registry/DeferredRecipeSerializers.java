package com.mowmaster.mowlib.Registry;

import com.mowmaster.mowlib.Recipes.InWorldDualHandedCrafting;
import net.minecraft.core.Registry;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nonnull;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;


public final class DeferredRecipeSerializers
{
/*
    @Nonnull
    public static final DeferredRegister<RecipeSerializer<?>> RECIPES = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<RecipeSerializer<InWorldDualHandedCrafting>> HAMMERING = RECIPES.register("dualhandedcrafting", InWorldDualHandedCrafting.Serializer::new);

    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(Registry.RECIPE_TYPE_REGISTRY, MODID);
    public static final RegistryObject<RecipeType<InWorldDualHandedCrafting>> INWORLD_HAMMERING = RECIPE_TYPES.register("dualhandedcrafting", () -> new RecipeType<>() {});
*/

    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, MODID);

    public static final RegistryObject<RecipeSerializer<InWorldDualHandedCrafting>> DUAL_HANDED_RECIPE_SERIALIZER =
            SERIALIZERS.register("dualhandedcrafting", () -> InWorldDualHandedCrafting.Serializer.INSTANCE);

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
