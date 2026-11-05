package com.mowmaster.mowlib.recipes;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.storage.StorageInteractions;
import net.minecraft.core.Registry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// Runs DualHandedRecipes: right-clicking a block with the right items in both hands makes the result, uses up the
// inputs the recipe says to, and either destroys the block (consume_block) or plays its hit sound. Both sides read the recipes (they're synced to clients),
// so the client also knows the click was a recipe and doesn't use the block itself. @EventBusSubscriber puts this on
// the game event bus.
@EventBusSubscriber(modid = MowLibReferences.MODID)
public final class DualHandedCrafting
{
    private DualHandedCrafting() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        // The event fires once per hand; only act on the main hand's turn
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        DualHandedRecipe recipe = find(level, player, state);
        if (recipe == null) return;

        if (level instanceof ServerLevel)
        {
            // consume() leaves creative players' stacks alone, like vanilla
            if (recipe.consumeOffhand()) player.getOffhandItem().consume(1, player);
            if (recipe.consumeMainhand()) player.getMainHandItem().consume(1, player);
            // Into the inventory, or dropped at the player's feet if it's full
            StorageInteractions.giveToPlayer(player, recipe.result().create());
            if (recipe.consumeBlock())
            {
                // destroyBlock: removed with its break sound and particles; false: it drops nothing (it was used up)
                level.destroyBlock(event.getPos(), false, player);
            }
            else
            {
                SoundType sound = state.getSoundType();
                level.playSound(null, event.getPos(), sound.getHitSound(), SoundSource.BLOCKS, 1f, 0.8f);
            }
        }
        // Cancel on both sides so nothing else happens with the click; SUCCESS swings the arm
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

    // The first recipe that matches, or null. registryAccess().lookup: the level's registries, which hold the datapack
    // ones (empty if it isn't there, e.g. a client that joined a server without it).
    public static @Nullable DualHandedRecipe find(Level level, Player player, BlockState state)
    {
        Optional<Registry<DualHandedRecipe>> recipes = level.registryAccess().lookup(DualHandedRecipe.KEY);
        if (recipes.isEmpty()) return null;
        for (DualHandedRecipe recipe : recipes.get())
        {
            if (recipe.matches(player, state)) return recipe;
        }
        return null;
    }
}
