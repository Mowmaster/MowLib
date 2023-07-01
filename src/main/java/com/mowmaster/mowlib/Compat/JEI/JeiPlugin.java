package com.mowmaster.mowlib.Compat.JEI;

import com.mowmaster.mowlib.Compat.JEI.recipes.*;
import com.mowmaster.mowlib.Recipes.*;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IStackHelper;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;
//Example from
//https://github.com/klikli-dev/occultism/blob/version/1.19/src/main/java/com/github/klikli_dev/occultism/integration/jei/JeiPlugin.java

@mezz.jei.api.JeiPlugin
public class JeiPlugin implements IModPlugin
{
    protected static IJeiRuntime runtime;

    public static IJeiRuntime getJeiRuntime() {
        return runtime;
    }

    @Override
    public ResourceLocation getPluginUid() {
        return new ResourceLocation(MODID, "jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new DualHandedCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new ToolSwapCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MachineBaseCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new WorkstationBaseCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new EffectCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new BadEffectCraftingRecipeCategory(registration.getJeiHelpers().getGuiHelper()));
        registration.addRecipeCategories(new MobFilterCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        ClientLevel level = Minecraft.getInstance().level;
        RecipeManager recipeManager = level.getRecipeManager();

        List<InWorldDualHandedCrafting> dualHandedRecipes = recipeManager.getAllRecipesFor(InWorldDualHandedCrafting.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.DUAL_HANDED_CRAFING, dualHandedRecipes);
        List<ToolSwapCrafting> toolSwapRecipes = recipeManager.getAllRecipesFor(ToolSwapCrafting.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.TOOL_SWAP_CRAFING, toolSwapRecipes);
        List<MachineBaseTypeRecipe> machineBase = recipeManager.getAllRecipesFor(MachineBaseTypeRecipe.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.MACHINEBASE_CRAFING, machineBase);
        List<WorkStationBaseTypeRecipe> workstationBase = recipeManager.getAllRecipesFor(WorkStationBaseTypeRecipe.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.WORKSTATIONBASE_CRAFING, workstationBase);
        List<MobEffectColorRecipe> effectGood = recipeManager.getAllRecipesFor(MobEffectColorRecipe.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.EFFECT_CRAFING, effectGood);
        List<MobEffectColorRecipeCorrupted> effectBad = recipeManager.getAllRecipesFor(MobEffectColorRecipeCorrupted.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.BADEFFECT_CRAFING, effectBad);
        List<BaseBlockEntityFilter> mobFilter = recipeManager.getAllRecipesFor(BaseBlockEntityFilter.Type.INSTANCE);
        registration.addRecipes(JEIRecipeTypes.MOB_FILTER, mobFilter);



        this.registerIngredientInfo(registration, DeferredRegisterItems.COLOR_APPLICATOR.get());
        this.registerIngredientInfo(registration, DeferredRegisterItems.SCROLL_T2_REPAIR.get());
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        IStackHelper stackHelper = registration.getJeiHelpers().getStackHelper();
        IRecipeTransferHandlerHelper handlerHelper = registration.getTransferHelper();
        /*
        registration.addUniversalRecipeTransferHandler(new StorageControllerRecipeTransferHandler<>(
                StorageControllerContainer.class, handlerHelper));
        */
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.ICON_HAND.get()),
                JEIRecipeTypes.DUAL_HANDED_CRAFING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.ICON_HAND.get()),
                JEIRecipeTypes.TOOL_SWAP_CRAFING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.ICON_MACHINEBASE.get()),
                JEIRecipeTypes.MACHINEBASE_CRAFING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.ICON_WORKSTATIONBASE.get()),
                JEIRecipeTypes.WORKSTATIONBASE_CRAFING);
        registration.addRecipeCatalyst(VanillaTypes.ITEM_STACK,new ItemStack(DeferredRegisterItems.ICON_DUST.get()),
                JEIRecipeTypes.EFFECT_CRAFING);
        registration.addRecipeCatalyst(new ItemStack(DeferredRegisterItems.ICON_DUST.get()),
                JEIRecipeTypes.BADEFFECT_CRAFING);
        registration.addRecipeCatalyst(new ItemStack(Items.SKELETON_SKULL).setHoverName(Component.translatable(MODID + ".jei.mob_filter_icon")),
                JEIRecipeTypes.MOB_FILTER);
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime jeiRuntime) {
        JeiPlugin.runtime = jeiRuntime;
        JEISettings.setJeiLoaded(true);
    }

    public void registerIngredientInfo(IRecipeRegistration registration, ItemLike ingredient) {
        registration.addIngredientInfo(new ItemStack(ingredient.asItem()), VanillaTypes.ITEM_STACK,
                Component.translatable("jei." + MODID + ".item." + ForgeRegistries.ITEMS.getKey(ingredient.asItem()).getPath() + ".description"));
    }

}
