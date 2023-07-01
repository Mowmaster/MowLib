package com.mowmaster.mowlib.Recipes;

import com.google.gson.JsonObject;
import com.mowmaster.mowlib.MowLibUtils.MowLibCompoundTagUtils;
import com.mowmaster.mowlib.Registry.DeferredRecipeSerializers;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Registry;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.crafting.CraftingHelper;
import net.minecraftforge.registries.ObjectHolder;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;


public class InWorldDualHandedCrafting implements Recipe<Container>
{
    @ObjectHolder(registryName = "forge:recipe_serializer", value = MODID + ":dualhandedcrafting")
    //public static RecipeSerializer<?> SERIALIZER = new Serializer();
    //public static RecipeType<InWorldDualHandedCrafting> INWORLD_HAMMERING = RecipeType.register(MODID + ":dualhandedcrafting");

    private final String group;
    private final ResourceLocation id;
    @Nullable
    private final Ingredient blockTarget;
    private final Ingredient offhandTool;
    private final Ingredient mainhandTool;
    private final ItemStack resultStack;
    private final Boolean consumeOffhandItem;
    private final Boolean consumeMainhandItem;

    private final Boolean modifyMainhandItem;
    private final Boolean modifyOffhandItem;
    private final String resultNBTName;
    private final int resultNBTValue;
    private final int resultNBTMinValue;
    private final int resultNBTMaxValue;



    //Someday add in a bool value to set as something fake players could do too or not???

    public InWorldDualHandedCrafting(ResourceLocation id, String group, @Nullable Ingredient blockTarget, @Nullable Ingredient mainhandTool, @Nullable Boolean consumeMainhandItem, @Nullable Ingredient offhandTool, @Nullable Boolean consumeOffhandItem, @Nullable ItemStack resultStack, @Nullable Boolean modifyMainhandItem, @Nullable Boolean modifyOffhandItem, @Nullable String resultNBTName, @Nullable int resultNBTValue, @Nullable int resultNBTMinValue, @Nullable int resultNBTMaxValue)
    {
        this.group = group;
        this.id = id;
        this.blockTarget = blockTarget;
        this.mainhandTool = mainhandTool;
        this.consumeMainhandItem = consumeMainhandItem;
        this.offhandTool = offhandTool;
        this.consumeOffhandItem = consumeOffhandItem;
        this.resultStack = resultStack;


        this.modifyMainhandItem = modifyMainhandItem;
        this.modifyOffhandItem = modifyOffhandItem;
        this.resultNBTName = resultNBTName;
        this.resultNBTValue = resultNBTValue;
        this.resultNBTMinValue = resultNBTMinValue;
        this.resultNBTMaxValue = resultNBTMaxValue;
    }

    @Override
    public boolean isSpecial() {
        return true;
    }

    public static Collection<InWorldDualHandedCrafting> getAllRecipes(Level world)
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
        allIngredients.add(blockTarget != null ? blockTarget : Ingredient.EMPTY);
        allIngredients.add(mainhandTool != null ? mainhandTool : Ingredient.EMPTY);
        allIngredients.add(offhandTool != null ? offhandTool : Ingredient.EMPTY);
        return allIngredients;
    }

    @Override
    public boolean matches(Container inv, Level worldIn)
    {
        //TargetBlock
        //MainHand
        //OffHand
        if(inv.getContainerSize()<3)return false;
        return offhandTool.test(inv.getItem(2)) && mainhandTool.test(inv.getItem(1)) && blockTarget.test(inv.getItem(0));
    }

    @Override
    public ItemStack assemble(Container inv)
    {
        if(resultStack == null || resultStack.isEmpty())
        {
            if(modifyMainHand())
            {
                ItemStack main = inv.getItem(1).copy();
                if(getResultModificationName() != null && getResultModificationName() != "")
                {
                    if(mainhandTool.test(main))
                    {
                        if(getNBTModifiedItem(main, true))
                        {
                            getNBTModifiedItem(main, false);
                            return main;
                        }
                        else{return main;}
                    }
                    else{return main;}
                }
                else{return main;}
            }
            else if(modifyOffHand())
            {
                ItemStack off = inv.getItem(2).copy();
                if(getResultModificationName() != null && getResultModificationName() != "")
                {
                    if(offhandTool.test(off))
                    {
                        if(getNBTModifiedItem(off, true))
                        {
                            getNBTModifiedItem(off, false);
                            return off;
                        }
                        else{return off;}
                    }
                    else{return off;}
                }
                else{return off;}
            }
        }

        return getResultItem().copy();
    }

    public boolean getNBTModifiedItem(ItemStack inputUpgradeStack, boolean simulate)
    {
        if(resultStack == null || resultStack.isEmpty())
        {
            if(getResultModificationName() != null && getResultModificationName() != "")
            {
                int value = MowLibCompoundTagUtils.readIntegerFromNBT(inputUpgradeStack.getOrCreateTag(),getResultModificationName());
                if(value>=getResultModificationMaxAmount()) { return false; }
                if(value<getResultModificationMinAmount()) { return false; }

                int newValue = value + getResultModificationAmount();
                if(newValue > getResultModificationMaxAmount())
                {
                    newValue = getResultModificationMaxAmount();
                }

                if(!simulate)MowLibCompoundTagUtils.writeIntegerToNBT(inputUpgradeStack.getOrCreateTag(),newValue,getResultModificationName());
                return true;
            }
        }

        return false;
    }

    @Override
    public ItemStack getResultItem()
    {
        return (resultStack.isEmpty() || resultStack == null)?(ItemStack.EMPTY):(resultStack);
    }

    public Boolean consumeMainHand(){return consumeMainhandItem;}

    public Boolean consumeOffHand(){return consumeOffhandItem;}

    public Boolean modifyMainHand(){return modifyMainhandItem;}
    public Boolean modifyOffHand(){return modifyOffhandItem;}
    public String getResultModificationName()
    {
        return resultNBTName;
    }
    public int getResultModificationAmount()
    {
        return resultNBTValue;
    }
    public int getResultModificationMinAmount()
    {
        return resultNBTMinValue;
    }
    public int getResultModificationMaxAmount()
    {
        return resultNBTMaxValue;
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

    public static class Type implements RecipeType<InWorldDualHandedCrafting> {
        private Type() { }
        public static final Type INSTANCE = new Type();
        public static final String ID = "dualhandedcrafting";
    }

    @Override
    public ItemStack getToastSymbol()
    {
        return new ItemStack(DeferredRegisterItems.ICON_HAND.get());
    }

    public Ingredient getPattern()
    {
        return blockTarget != null ? blockTarget : Ingredient.EMPTY;
    }

    public static class Serializer implements RecipeSerializer<InWorldDualHandedCrafting> {
        public static final Serializer INSTANCE = new Serializer();
        public static final ResourceLocation ID =
                new ResourceLocation(MODID,"dualhandedcrafting");


        protected InWorldDualHandedCrafting createRecipe(ResourceLocation recipeId, String group, @Nullable Ingredient blockTarget, @Nullable Ingredient mainhandTool, @Nullable Boolean consumeMainhandItem, @Nullable Ingredient offhandTool, @Nullable Boolean consumeOffhandItem, @Nullable ItemStack resultStack, @Nullable Boolean modifyMainhandItem, @Nullable Boolean modifyOffhandItem, @Nullable String resultNBTName, @Nullable int resultNBTValue, @Nullable int resultNBTMinValue, @Nullable int resultNBTMaxValue)
        {
            return new InWorldDualHandedCrafting(recipeId, group, blockTarget, mainhandTool, consumeMainhandItem, offhandTool, consumeOffhandItem, resultStack, modifyMainhandItem, modifyOffhandItem, resultNBTName, resultNBTValue, resultNBTMinValue, resultNBTMaxValue);
        }

        @Override
        public InWorldDualHandedCrafting fromJson(ResourceLocation recipeId, JsonObject json)
        {
            String group = GsonHelper.getAsString(json, "group", "");
            Ingredient blockTarget = json.has("blockTarget") ? CraftingHelper.getIngredient(json.get("blockTarget")) : null;
            Ingredient mainhandTool = json.has("mainhandTool") ? CraftingHelper.getIngredient(json.get("mainhandTool")) : null;
            Boolean consumeMainhandItem = json.has("consumeMainhandItem") ? GsonHelper.getAsBoolean(json,"consumeMainhandItem") : true;
            Ingredient offhandTool = json.has("offhandTool") ? CraftingHelper.getIngredient(json.get("offhandTool")) : null;
            Boolean consumeOffhandItem = json.has("consumeOffhandItem") ? GsonHelper.getAsBoolean(json,"consumeOffhandItem") : true;
            ItemStack resultStack = json.has("result") ? CraftingHelper.getItemStack(GsonHelper.getAsJsonObject(json, "result"), true) : ItemStack.EMPTY;

            Boolean modifyMainhandItem = json.has("modifyMainhandItem") ? GsonHelper.getAsBoolean(json,"modifyMainhandItem") : false;
            Boolean modifyOffhandItem = json.has("modifyOffhandItem") ? GsonHelper.getAsBoolean(json,"modifyOffhandItem") : false;
            String resultNBTName = json.has("resultNBTName") ? GsonHelper.getAsString(json, "resultNBTName", "") : "";
            int resultNBTValue = json.has("resultNBTValue") ? GsonHelper.getAsInt(json,"resultNBTValue") : (0);
            int resultNBTMinValue = json.has("resultNBTMinValue") ? GsonHelper.getAsInt(json,"resultNBTMinValue") : (0);
            int resultNBTMaxValue = json.has("resultNBTMaxValue") ? GsonHelper.getAsInt(json,"resultNBTMaxValue") : (0);
            return createRecipe(recipeId, group, blockTarget, mainhandTool, consumeMainhandItem, offhandTool, consumeOffhandItem, resultStack, modifyMainhandItem, modifyOffhandItem, resultNBTName, resultNBTValue, resultNBTMinValue, resultNBTMaxValue);
        }


        @Override
        public InWorldDualHandedCrafting fromNetwork(ResourceLocation recipeId, FriendlyByteBuf buffer)
        {
            String group = buffer.readUtf(32767);
            boolean hasInput = buffer.readBoolean();
            Ingredient blockTarget = hasInput ? Ingredient.fromNetwork(buffer) : null;
            boolean hasMainHandTool = buffer.readBoolean();
            Ingredient mainhandTool = hasMainHandTool ? Ingredient.fromNetwork(buffer) : null;
            boolean consumeMainhandItem = buffer.readBoolean();
            boolean hasOffHandTool = buffer.readBoolean();
            Ingredient offhandTool = hasOffHandTool ? Ingredient.fromNetwork(buffer) : null;
            boolean consumeOffhandItem = buffer.readBoolean();
            ItemStack result = buffer.readItem();
            boolean modifyMainhandItem = buffer.readBoolean();
            boolean modifyOffhandItem = buffer.readBoolean();
            String resultNBTName = buffer.readUtf(32767);
            int resultNBTValue = buffer.readInt();
            int resultNBTMinValue = buffer.readInt();
            int resultNBTMaxValue = buffer.readInt();
            return createRecipe(recipeId, group,  blockTarget, mainhandTool, consumeMainhandItem, offhandTool, consumeOffhandItem, result,modifyMainhandItem,modifyOffhandItem,resultNBTName,resultNBTValue,resultNBTMinValue,resultNBTMaxValue);
        }

        @Override
        public void toNetwork(FriendlyByteBuf buffer, InWorldDualHandedCrafting recipe)
        {
            buffer.writeUtf(recipe.group);
            boolean hasInput = recipe.blockTarget != null;
            buffer.writeBoolean(hasInput);
            if (hasInput) recipe.blockTarget.toNetwork(buffer);
            boolean hasMainHandTool = recipe.mainhandTool != null;
            buffer.writeBoolean(hasMainHandTool);
            if (hasMainHandTool) recipe.mainhandTool.toNetwork(buffer);
            buffer.writeBoolean(recipe.consumeMainhandItem);
            boolean hasOffHandTool = recipe.offhandTool != null;
            buffer.writeBoolean(hasOffHandTool);
            if (hasOffHandTool) recipe.offhandTool.toNetwork(buffer);
            buffer.writeBoolean(recipe.consumeOffhandItem);
            buffer.writeItem(recipe.resultStack);
            buffer.writeBoolean(recipe.modifyMainhandItem);
            buffer.writeBoolean(recipe.modifyOffhandItem);
            buffer.writeUtf(recipe.resultNBTName);
            buffer.writeInt(recipe.resultNBTValue);
            buffer.writeInt(recipe.resultNBTMinValue);
            buffer.writeInt(recipe.resultNBTMaxValue);
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