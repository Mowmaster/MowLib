package com.mowmaster.mowlib.knapping;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.storage.StorageInteractions;
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

// Runs KnappingRecipes: right-clicking a block with the right items in both hands makes the result, uses up the
// held items the recipe says to, and plays the block's hit sound. @EventBusSubscriber puts this on the game event bus.
// (Moved from Bibliomania's KnappingHandler, with the recipes taken out into KnappingRecipes.)
@EventBusSubscriber(modid = MowLibReferences.MODID)
public final class KnappingHandler
{
    private KnappingHandler() {}

    @SubscribeEvent
    public static void onRightClickBlock(PlayerInteractEvent.RightClickBlock event)
    {
        // The event fires once per hand; only act on the main hand's turn
        if (event.getHand() != InteractionHand.MAIN_HAND) return;
        Player player = event.getEntity();
        Level level = event.getLevel();
        BlockState state = level.getBlockState(event.getPos());
        KnappingRecipe recipe = KnappingRecipes.find(player, state);
        if (recipe == null) return;

        if (level instanceof ServerLevel)
        {
            // consume() leaves creative players' stacks alone, like vanilla
            if (recipe.consumeMainhand()) player.getMainHandItem().consume(1, player);
            if (recipe.consumeOffhand()) player.getOffhandItem().consume(1, player);
            // Into the inventory, or dropped at the player's feet if it's full (MowLib's shared helper)
            StorageInteractions.giveToPlayer(player, recipe.result().get());
            SoundType sound = state.getSoundType();
            level.playSound(null, event.getPos(), sound.getHitSound(), SoundSource.BLOCKS, 1f, 0.8f);
        }
        // Cancel on both sides so nothing else happens with the click; SUCCESS swings the arm
        event.setCanceled(true);
        event.setCancellationResult(InteractionResult.SUCCESS);
    }
}
