package com.mowmaster.mowlib.util;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.ChorusFlowerBlock;
import net.minecraft.world.level.block.GrowingPlantBlock;
import net.minecraft.world.level.block.VegetationBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.CommonHooks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// Breaking and placing blocks as a (fake) player, the way a real player would, so other mods' protections and
// events apply. Use with MowLibFakePlayer.
public final class MowLibBlockActions
{
    private MowLibBlockActions()
    {
    }

    // Breaks pos as player, with the tool in its main hand. Drops and experience appear at dropAt (the block itself,
    // or somewhere else such as the machine). Fires NeoForge's break event first, so protection mods can stop it.
    // Wears the held tool down. false if nothing broke.
    public static boolean breakBlock(ServerLevel level, Player player, BlockPos pos, BlockPos dropAt)
    {
        BlockState state = level.getBlockState(pos);
        if (CommonHooks.fireBlockBreak(level, GameType.SURVIVAL, player, pos, state).isCanceled()) return false;

        BlockEntity blockEntity = level.getBlockEntity(pos);
        ItemStack tool = player.getMainHandItem();
        boolean harvest = !state.requiresCorrectToolForDrops() || player.hasCorrectToolForDrops(state);
        List<ItemStack> drops = harvest ? Block.getDrops(state, level, pos, blockEntity, player, tool) : List.of();
        int experience = harvest ? state.getExpDrop(level, pos, blockEntity, player, tool) : 0;

        // Removes the block (and its block entity) the way a player breaking it does
        if (!state.onDestroyedByPlayer(level, pos, player, tool, harvest, level.getFluidState(pos))) return false;
        state.getBlock().destroy(level, pos, state);
        if (!tool.isEmpty()) tool.mineBlock(level, state, pos, player);

        drops.forEach(drop -> Block.popResource(level, dropAt, drop));
        if (experience > 0) ExperienceOrb.award(level, Vec3.atCenterOf(dropAt), experience);
        return true;
    }

    // Places one of stack at pos as player (the item's own placing code runs, with NeoForge's place event).
    // pos must be air or replaceable. Places a one-item copy and doesn't change stack: on true, the caller removes
    // one from its own stack. The player's main hand is used for the placing and left empty afterwards.
    public static boolean placeBlock(ServerLevel level, Player player, ItemStack stack, BlockPos pos)
    {
        ItemStack held = stack.copyWithCount(1);
        player.setItemInHand(InteractionHand.MAIN_HAND, held);
        try
        {
            // Clicking the target block itself: placing into an air/replaceable block puts the new block right there
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
            InteractionResult result = CommonHooks.onPlaceItemIntoWorld(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
            return result.consumesAction() && held.isEmpty();
        }
        finally
        {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        }
    }

    // Plants, saplings, chorus flowers and vines
    public static boolean isPlant(Block block)
    {
        return block instanceof VegetationBlock || block instanceof ChorusFlowerBlock || block instanceof GrowingPlantBlock;
    }

    // The block's "age" property, if it has one (crops, stems, berry bushes...; 1.20's getBlockPropertyAge)
    public static @Nullable IntegerProperty ageProperty(BlockState state)
    {
        for (Property<?> property : state.getProperties())
        {
            if (property instanceof IntegerProperty integer && property.getName().equalsIgnoreCase("age")) return integer;
        }
        return null;
    }
}
