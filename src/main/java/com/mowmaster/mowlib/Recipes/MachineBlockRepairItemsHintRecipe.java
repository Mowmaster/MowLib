package com.mowmaster.mowlib.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MachineBlockRepairItemsHintRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":machine_repair_item_hints")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient input;
    private final ItemStack output;
    private final String title;
    private final String description;
    private final boolean localized;

    public MachineBlockRepairItemsHintRecipe(ResourceLocation id, String group, @Nullable Ingredient input, ItemStack output, String title, String description, boolean localized)
    {
        this.group = group;
        this.id = id;
        this.input = input;
        this.output = output;
        this.title = title;
        this.description = description;
        this.localized = localized;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<MachineBlockRepairItemsHintRecipe> getAllRecipes(Level world)
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
        allIngredients.add(input != null ? input : Ingredient.EMPTY);
        return allIngredients;
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        ItemStack inputStack = inv.getItem(0);
        return input.test(inputStack);
    }

    @Override
    public ItemStack assemble(Container p_44001_, RegistryAccess p_267165_)
    {
        return getResultItem(p_267165_).copy();
    }

    @Override
    public ItemStack getResultItem(RegistryAccess p_267052_)
    {
        return output;
    }

    public ItemStack getResultItemJEI()
    {
        return output;
    }

    public String getResultTitle() {return title;}

    public String getResultDescription() {return description;}

    public boolean getResultLocalized() {return localized;}

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

    public static class Type implements RecipeType<MachineBlockRepairItemsHintRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "machine_repair_item_hints";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.COLOR_APPLICATOR.get());
    }

    public Ingredient getPattern()
    {
        return input != null ? input : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<MachineBlockRepairItemsHintRecipe>
    {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"machine_repair_item_hints");

        protected MachineBlockRepairItemsHintRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient input, ItemStack result, String title, String description, boolean localized)
        {
            return new MachineBlockRepairItemsHintRecipe(recipeId, group, input, result, title, description, localized);
        }

        @Override
        public MachineBlockRepairItemsHintRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient input = json.has("input") ? CraftingHelper.getIngredient(json.get("input"),false) : null;
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            String title = json.has("hintTitle") ? GsonHelper.getAsString(json,"hintTitle") : "";
            String description = json.has("hintDescription") ? GsonHelper.getAsString(json,"hintDescription") : "";
            boolean localized = json.has("localized") ? GsonHelper.getAsBoolean(json,"localized") : false;
            return createRecipe(recipeId, group, input, result, title, description, localized);
        }

        @Override
        public MachineBlockRepairItemsHintRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient input = hasInput ? Ingredient.fromNetwork(buffer) : null;
            ItemStack result = buffer.readItem();
            String title = buffer.readUtf(32767);
            String description = buffer.readUtf(32767);
            boolean localized = buffer.readBoolean();
            return createRecipe(recipeId, group,  input, result, title, description, localized);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MachineBlockRepairItemsHintRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInput = recipe.input != null;
            buffer.writeBoolean(hasInput);
            if (hasInput) recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeUtf(recipe.title);
            buffer.writeUtf(recipe.description);
            buffer.writeBoolean(recipe.localized);
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
