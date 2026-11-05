package com.mowmaster.mowlib.recipes;

import com.mowmaster.mowlib.picker.ItemPicker;
import com.mowmaster.mowlib.picker.PickerGroup;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;

import java.util.Optional;

// Any picker group's item alone in the crafting grid -> that group's blank (its settings and data are gone with it):
// a filter -> a Blank Filter, a work card -> a Blank Work Card, and the same for every group another mod registers
// with resetRecipe on (picker/ItemPicker). Serializer: mowlib:picker_reset.
public class PickerResetRecipe extends CustomRecipe
{
    public static final PickerResetRecipe INSTANCE = new PickerResetRecipe();

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        return groupOf(input).isPresent();
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        return groupOf(input).map(group -> new ItemStack(group.base().get())).orElse(ItemStack.EMPTY);
    }

    @Override
    public RecipeSerializer<PickerResetRecipe> getSerializer()
    {
        return MowLibRecipes.PICKER_RESET.get();
    }

    // The group of the single item in the grid, or empty if the grid holds anything else (or more than one thing)
    private static Optional<PickerGroup> groupOf(CraftingInput input)
    {
        ItemStack found = null;
        for (int slot = 0; slot < input.size(); slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (found != null) return Optional.empty();
            found = stack;
        }
        return found == null ? Optional.empty() : ItemPicker.forReset(found.getItem());
    }
}
