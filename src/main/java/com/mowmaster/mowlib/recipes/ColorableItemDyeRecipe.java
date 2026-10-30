package com.mowmaster.mowlib.recipes;

import com.mowmaster.mowlib.MowLibIds;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CustomRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// Dyeing an item in the crafting grid: one item from #mowlib:dyeable_in_crafting plus one or more dyes (vanilla dyes
// or Custom Dyes, see IColorableBlock#dyeColorOf), anywhere in the grid -> the same item (all its data kept) with the
// dyes mixed into its minecraft:dyed_color, the same way leather armour mixes. A colourable block's item carries
// region 0's colour in dyed_color (CBTintBlockEntity / CBTintData), so the placed block shows it.
// Mods opt their items in with the tag, so no mod's blocks become dyeable in crafting by surprise.
// (Generalised from Bibliomania's BookDyeRecipe.) Serializer: mowlib:colorable_dye.
public class ColorableItemDyeRecipe extends CustomRecipe
{
    public static final ColorableItemDyeRecipe INSTANCE = new ColorableItemDyeRecipe();

    public static final TagKey<Item> DYEABLE_IN_CRAFTING = TagKey.create(Registries.ITEM, MowLibReferences.id("dyeable_in_crafting"));

    @Override
    public boolean matches(CraftingInput input, Level level)
    {
        return findTarget(input) != null;
    }

    @Override
    public ItemStack assemble(CraftingInput input)
    {
        ItemStack target = findTarget(input);
        if (target == null) return ItemStack.EMPTY;

        DyedItemColor current = target.get(DataComponents.DYED_COLOR);
        int color = current != null ? current.rgb() : CBTintBlockEntity.UNDYED;
        for (int slot = 0; slot < input.size(); slot++)
        {
            Integer dye = IColorableBlock.dyeColorOf(input.getItem(slot));
            if (dye != null) color = IColorableBlock.mixDye(color, dye);
        }

        ItemStack result = target.copyWithCount(1);
        result.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
        return result;
    }

    @Override
    public RecipeSerializer<ColorableItemDyeRecipe> getSerializer()
    {
        return MowLibRecipes.COLORABLE_DYE.get();
    }

    // The single dyeable item, if the grid holds exactly one plus at least one dye and nothing else
    @Nullable
    private static ItemStack findTarget(CraftingInput input)
    {
        ItemStack target = null;
        boolean hasDye = false;
        for (int slot = 0; slot < input.size(); slot++)
        {
            ItemStack stack = input.getItem(slot);
            if (stack.isEmpty()) continue;
            if (stack.is(DYEABLE_IN_CRAFTING))
            {
                if (target != null) return null;
                target = stack;
            }
            else if (IColorableBlock.dyeColorOf(stack) != null)
            {
                hasDye = true;
            }
            else
            {
                return null;
            }
        }
        return hasDye ? target : null;
    }
}
