package com.mowmaster.mowlib.recipes;

import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

// Any filter alone in the crafting grid -> a Blank Filter (its settings and list are gone with it). With the Blank
// Filter's radial menu (FilterPicker) this turns any filter into any other. Works for every MowLib filter
// (ITransportFilter), from any mod. Serializer: mowlib:filter_reset.
public class FilterResetRecipe extends CustomRecipe
{
    public static final FilterResetRecipe INSTANCE = new FilterResetRecipe();

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        return findFilter(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        return findFilter(input) == null ? ItemStack.EMPTY : new ItemStack(MowLibItems.FILTER_BASE.get());
    }

    @Override
    public RecipeSerializer<FilterResetRecipe> getSerializer()
    {
        return MowLibRecipes.FILTER_RESET.get();
    }

    // The single filter, or null if the grid holds anything else (or more than one thing)
    private static ItemStack findFilter(CraftingInput input)
    {
        ItemStack filter = null;
        for (int slot = 0; slot < input.size(); slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (filter != null || !(stack.getItem() instanceof ITransportFilter)) return null;
            filter = stack;
        }
        return filter;
    }
}
