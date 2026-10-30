package com.mowmaster.mowlib.recipes;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import org.jetbrains.annotations.Nullable;

// A colour picker holding a colour, alone in the grid -> a Custom Dye of that colour, named after the block it was
// picked from (the picker comes back reset)
public class CBPickedDyeRecipe extends CustomRecipe
{
    public static final CBPickedDyeRecipe INSTANCE = new CBPickedDyeRecipe();

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        return findPicker(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        ItemStack picker = findPicker(input);
        if (picker == null) return ItemStack.EMPTY;

        ItemStack dye = new ItemStack(MowLibItems.CUSTOM_DYE.get());
        dye.set(DataComponents.DYED_COLOR, picker.get(DataComponents.DYED_COLOR));
        // Named after the block the colour came from: "Diamond Block Dye". ITEM_NAME replaces the item's default
        // name (not shown in italics, unlike an anvil name, CUSTOM_NAME). The block's name is a translatable
        // component, so it shows in each player's own language. Pickers from before this change have no block.
        Block block = picker.get(MowLibDataComponents.PICKED_BLOCK.get());
        if (block != null)
        {
            dye.set(DataComponents.ITEM_NAME, Component.translatable("item." + MowLibReferences.MODID + ".custom_dye.from_block", block.getName()));
        }
        return dye;
    }

    @Override
    public RecipeSerializer<CBPickedDyeRecipe> getSerializer()
    {
        return MowLibRecipes.PICKED_DYE.get();
    }

    // The single picker with a stored colour, or null if the grid holds anything else
    @Nullable
    private static ItemStack findPicker(CraftingInput input)
    {
        ItemStack picker = null;
        for (int slot = 0; slot < input.size(); slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (picker != null || !stack.is(MowLibItems.COLOR_PICKER.get()) || !stack.has(DataComponents.DYED_COLOR)) return null;
            picker = stack;
        }
        return picker;
    }
}
