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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;


public class MachineBlockRenderItemsRecipe implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":machine_render_items")

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient input;
    private final ItemStack output;
    private final float transitionX;
    private final float transitionY;
    private final float transitionZ;
    private final float scaleX;
    private final float scaleY;
    private final float scaleZ;
    private final float angle;
    private final boolean renderItem;
    private final boolean renderAsBlock;

    public MachineBlockRenderItemsRecipe(ResourceLocation id, String group, @Nullable Ingredient input, ItemStack output, float transitionX, float transitionY, float transitionZ, float scaleX, float scaleY, float scaleZ, float angle, boolean renderItem, boolean renderAsBlock)
    {
        this.group = group;
        this.id = id;
        this.input = input;
        this.output = output;
        this.transitionX = transitionX;
        this.transitionY = transitionY;
        this.transitionZ = transitionZ;
        this.scaleX = scaleX;
        this.scaleY = scaleY;
        this.scaleZ = scaleZ;
        this.angle = angle;
        this.renderItem = renderItem;
        this.renderAsBlock = renderAsBlock;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<MachineBlockRenderItemsRecipe> getAllRecipes(Level world)
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
        ItemStack inputStackMachine = inv.getItem(0);
        ItemStack inputStackItem = inv.getItem(1);
        return input.test(inputStackMachine) && input.test(inputStackItem);
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

    public List<Float> getResultList() {return Arrays.asList(transitionX, transitionY, transitionZ, scaleX, scaleY, scaleZ, angle);}

    public boolean getResultRenderItem()
    {
        return renderItem;
    }

    public boolean getResultRenderAsBlock()
    {
        return renderAsBlock;
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

    public static class Type implements RecipeType<MachineBlockRenderItemsRecipe> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "machine_render_items";
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

    public static class Serializer implements RecipeSerializer<MachineBlockRenderItemsRecipe>
    {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"machine_render_items");

        protected MachineBlockRenderItemsRecipe createRecipe(ResourceLocation recipeId, String group, Ingredient input, ItemStack result, float transitionX, float transitionY, float transitionZ, float scaleX, float scaleY, float scaleZ, float angle, boolean renderItem, boolean renderAsBlock)
        {
            return new MachineBlockRenderItemsRecipe(recipeId, group, input, result, transitionX, transitionY, transitionZ, scaleX, scaleY, scaleZ, angle, renderItem, renderAsBlock);
        }

        @Override
        public MachineBlockRenderItemsRecipe fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient input = json.has("input") ? CraftingHelper.getIngredient(json.get("input"),false) : null;
            ItemStack result = CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true);
            float transitionX = json.has("transitionX") ? GsonHelper.getAsFloat(json,"transitionX") : 0.0f;
            float transitionY = json.has("transitionY") ? GsonHelper.getAsFloat(json,"transitionY") : 0.0f;
            float transitionZ = json.has("transitionZ") ? GsonHelper.getAsFloat(json,"transitionZ") : 0.0f;
            float scaleX = json.has("scaleX") ? GsonHelper.getAsFloat(json,"scaleX") : 0.0f;
            float scaleY = json.has("scaleY") ? GsonHelper.getAsFloat(json,"scaleY") : 0.0f;
            float scaleZ = json.has("scaleZ") ? GsonHelper.getAsFloat(json,"scaleZ") : 0.0f;
            float angle = json.has("angle") ? GsonHelper.getAsFloat(json,"angle") : 0.0f;
            boolean renderItem = json.has("renderItem") ? GsonHelper.getAsBoolean(json,"renderItem") : false;
            boolean renderAsBlock = json.has("renderAsBlock") ? GsonHelper.getAsBoolean(json,"renderAsBlock") : false;
            return createRecipe(recipeId, group, input, result, transitionX, transitionY, transitionZ, scaleX, scaleY, scaleZ, angle, renderItem, renderAsBlock);
        }

        @Override
        public MachineBlockRenderItemsRecipe fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient input = hasInput ? Ingredient.fromNetwork(buffer) : null;
            ItemStack result = buffer.readItem();
            float transitionX = buffer.readFloat();
            float transitionY = buffer.readFloat();
            float transitionZ = buffer.readFloat();
            float scaleX = buffer.readFloat();
            float scaleY = buffer.readFloat();
            float scaleZ = buffer.readFloat();
            float angle = buffer.readFloat();
            boolean renderItem = buffer.readBoolean();
            boolean renderAsBlock = buffer.readBoolean();
            return createRecipe(recipeId, group,  input, result, transitionX, transitionY, transitionZ, scaleX, scaleY, scaleZ, angle, renderItem, renderAsBlock);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, MachineBlockRenderItemsRecipe recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInput = recipe.input != null;
            buffer.writeBoolean(hasInput);
            if (hasInput) recipe.input.toNetwork(buffer);
            buffer.writeItem(recipe.output);
            buffer.writeFloat(recipe.transitionX);
            buffer.writeFloat(recipe.transitionY);
            buffer.writeFloat(recipe.transitionZ);
            buffer.writeFloat(recipe.scaleX);
            buffer.writeFloat(recipe.scaleY);
            buffer.writeFloat(recipe.scaleZ);
            buffer.writeFloat(recipe.angle);
            buffer.writeBoolean(recipe.renderItem);
            buffer.writeBoolean(recipe.renderAsBlock);
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