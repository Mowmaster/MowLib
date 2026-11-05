package com.mowmaster.mowlib.keys;

import com.mowmaster.mowlib.storage.StorageInteractions;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;

// A block entity whose block can be picked up as its item, contents and all: by the pick-up key
// (MowLibKeyMappings.PICKUP, default H) for any of them, and by whatever click the block itself uses (call pickUp).
// Its block entity must not also drop the item when it's removed this way: markPickedUp is called first, so check it
// in preRemoveSideEffects (if (pickedUp) return;).
public interface IPickupableBlock
{
    // The block as an item, carrying its contents
    ItemStack toItem();

    // Called just before the block is removed for a pickup
    void markPickedUp();

    // Whether it can be picked up right now (by this player)
    default boolean canPickUp(Player player)
    {
        return true;
    }

    // Picks it up (server): removes the block, the item into an empty main hand or else the inventory, with the
    // block's break sound. Override for special cases (spilling some contents first...).
    default void pickUp(ServerLevel level, BlockPos pos, BlockState state, Player player)
    {
        ItemStack item = toItem();
        markPickedUp();
        level.removeBlock(pos, false);
        give(player, item);
        SoundType sound = state.getSoundType();
        level.playSound(null, pos, sound.getBreakSound(), SoundSource.BLOCKS, (sound.getVolume() + 1f) / 2f, sound.getPitch() * 0.8f);
    }

    // Picks up whatever pickupable block is at pos. true if there was one.
    static boolean pickUpAt(ServerLevel level, BlockPos pos, Player player)
    {
        if (!(level.getBlockEntity(pos) instanceof IPickupableBlock pickupable) || !pickupable.canPickUp(player)) return false;
        pickupable.pickUp(level, pos, level.getBlockState(pos), player);
        return true;
    }

    // Into an empty main hand, otherwise the inventory (or at the player's feet if that's full): never replaces what
    // the player is holding
    static void give(Player player, ItemStack item)
    {
        if (player.getMainHandItem().isEmpty()) player.setItemInHand(InteractionHand.MAIN_HAND, item);
        else StorageInteractions.giveToPlayer(player, item);
    }
}
