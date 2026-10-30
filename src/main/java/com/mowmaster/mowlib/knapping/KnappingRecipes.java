package com.mowmaster.mowlib.knapping;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Every mod's knapping recipes, used by KnappingHandler. Recipes are code, not data: register them from your mod's
// FMLCommonSetupEvent (it runs on both client and server, which both need them, and every item exists by then):
//
//     event.enqueueWork(() -> KnappingRecipes.register(KnappingRecipe.of(Items.FLINT, Items.FLINT,
//             BlockTags.LOGS, () -> new ItemStack(MyItems.KNIFE_HANDLE.get()))));
//
// The first recipe that matches wins.
public final class KnappingRecipes
{
    private KnappingRecipes() {}

    // CopyOnWriteArrayList: safe to read from the game thread while a mod is still adding to it during setup
    private static final List<KnappingRecipe> RECIPES = new CopyOnWriteArrayList<>();

    public static void register(KnappingRecipe recipe)
    {
        RECIPES.add(recipe);
    }

    @Nullable
    public static KnappingRecipe find(Player player, BlockState state)
    {
        for (KnappingRecipe recipe : RECIPES)
        {
            if (recipe.matches(player, state)) return recipe;
        }
        return null;
    }

    public static List<KnappingRecipe> all()
    {
        return List.copyOf(RECIPES);
    }
}
