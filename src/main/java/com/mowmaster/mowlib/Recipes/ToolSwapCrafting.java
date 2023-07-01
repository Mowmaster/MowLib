package com.mowmaster.mowlib.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Items.BaseUseInteractionItem;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Collection;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;


public class ToolSwapCrafting implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":toolswapcrafting")

    private final String group;
    private final ResourceLocation id;
    private final Ingredient mainhandTool;
    private final ItemStack resultTool;



    //Someday add in a bool value to set as something fake players could do too or not???

    public ToolSwapCrafting(ResourceLocation id, String group, @Nullable Ingredient mainhandTool, ItemStack resultTool)
    {
        this.group = group;
        this.id = id;
        this.mainhandTool = mainhandTool;
        this.resultTool = resultTool;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<ToolSwapCrafting> getAllRecipes(Level world)
    {
        return world.getRecipeManager().getAllRecipesFor(Type.INSTANCE);
    }

    @Override
    public String getGroup()
    {
        return group;
    }

    @Override
    public boolean canCraftInDimensions(int width, int height)
    {
        return true;
    }

    @Override
    public NonNullList<Ingredient> getIngredients()
    {
        NonNullList<Ingredient> allIngredients = NonNullList.create();
        allIngredients.add(mainhandTool != null ? mainhandTool : Ingredient.EMPTY);
        return allIngredients;
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        //TargetBlock
        //MainHand
        //OffHand
        if(inv.getContainerSize()<1)return false;
        if(Arrays.stream(mainhandTool.getItems()).anyMatch(stack -> !(stack.getItem() instanceof BaseUseInteractionItem)))return false;
        if(!(inv.getItem(0).getItem() instanceof BaseUseInteractionItem))return false;
        return mainhandTool.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_) {
        return assemble(p_44001_);
    }

    public ItemStack assemble(Container inv)
    {
        return getResultItem().copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_) {
        return getResultItem();
    }

    public ItemStack getResultItem()
    {
        return resultTool;
    }

    @Override
    public ResourceLocation getId()
    {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return Serializer.INSTANCE;
    }

    @Override
    public RecipeType<?> getType() {
        return Type.INSTANCE;
    }

    public static class Type implements RecipeType<ToolSwapCrafting> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "toolswapcrafting";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.ICON_HAND.get());
    }

    public Ingredient getPattern()
    {
        return mainhandTool != null ? mainhandTool : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<ToolSwapCrafting> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"toolswapcrafting");

        protected ToolSwapCrafting createRecipe(ResourceLocation recipeId, String group, Ingredient mainhandTool, ItemStack result)
        {
            return new ToolSwapCrafting(recipeId, group, mainhandTool, result);
        }

        @Override
        public ToolSwapCrafting fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient mainhandTool = json.has("mainhandTool") ? CraftingHelper.getIngredient(json.get("mainhandTool"),false) : null;
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            return createRecipe(recipeId, group, mainhandTool, result);
        }


        @Override
        public ToolSwapCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasMainHandTool = buffer.readBoolean();
            Ingredient mainhandTool = hasMainHandTool ? Ingredient.fromNetwork(buffer) : null;
            ItemStack result = buffer.readItem();
            return createRecipe(recipeId, group, mainhandTool, result);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, ToolSwapCrafting recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasMainHandTool = recipe.mainhandTool != null;
            buffer.writeBoolean(hasMainHandTool);
            if (hasMainHandTool) recipe.mainhandTool.toNetwork(buffer);
            buffer.writeItem(recipe.resultTool);
        }

        public RecipeSerializer<?> setRegistryName(ResourceLocation name) {
            return INSTANCE;
        }

        @Nullable
        public ResourceLocation getRegistryName() {
            return ID;
        }

        public Class<RecipeSerializer<?>> getRegistryType() {
            return Serializer.castClass(RecipeSerializer.class);
        }

        @SuppressWarnings("unchecked") // Need this wrapper, because generics
        private static <G> Class<G> castClass(Class<?> cls) {
            return (Class<G>)cls;
        }
    }
}