package com.mowmaster.mowlib.Registry;

import com.mowmaster.mowlib.Recipes.*;
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

    public static final RegistryObject<RecipeSerializer<ToolSwapCrafting>> TOOL_SWAP_RECIPE_SERIALIZER =
            SERIALIZERS.register("toolswapcrafting", () -> ToolSwapCrafting.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<BaseBlockEntityFilter>> ENTITY_FILTER_RECIPE_SERIALIZER =
            SERIALIZERS.register("entity_filter", () -> BaseBlockEntityFilter.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MachineBaseTypeRecipe>> MACHINE_BASE_TYPE_RECIPE_SERIALIZER =
            SERIALIZERS.register("machinebase", () -> MachineBaseTypeRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MachineBlockRenderItemsRecipe>> MACHINE_BASE_RENDER_RECIPE_SERIALIZER =
            SERIALIZERS.register("machine_render_items", () -> MachineBlockRenderItemsRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MachineBlockRepairItemsHintRecipe>> MACHINE_BASE_HINT_RECIPE_SERIALIZER =
            SERIALIZERS.register("machine_repair_item_hints", () -> MachineBlockRepairItemsHintRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MachineBlockRepairItemsRecipe>> MACHINE_BASE_REPAIR_RECIPE_SERIALIZER =
            SERIALIZERS.register("machine_repair_items", () -> MachineBlockRepairItemsRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MobEffectColorRecipe>> EFFECT_COLOR_RECIPE_SERIALIZER =
            SERIALIZERS.register("mobeffect_color", () -> MobEffectColorRecipe.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<MobEffectColorRecipeCorrupted>> CORRUPTED_EFFECT_COLOR_RECIPE_SERIALIZER =
            SERIALIZERS.register("mobeffectcorrupted_color", () -> MobEffectColorRecipeCorrupted.Serializer.INSTANCE);

    public static final RegistryObject<RecipeSerializer<WorkStationBaseTypeRecipe>> WORKSTATION_BASE_TYPE_RECIPE_SERIALIZER =
            SERIALIZERS.register("workstationbase", () -> WorkStationBaseTypeRecipe.Serializer.INSTANCE);


    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
    }
}
