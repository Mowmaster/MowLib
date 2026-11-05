package com.mowmaster.mowlib.keys;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;

// A held item that takes over the pick-up and place keys (MowLibKeyMappings.PICKUP / PLACE, default H / G) while
// looking at certain blocks: a filter adding the looked-at block to its list (pick up) or taking it off (place).
// Checked before the keys' normal jobs (picking up an IPickupableBlock, placing an IKeyPlaceable), main hand first,
// so the keys still work as usual everywhere else. usesBlockKey runs on both sides (client: whether to send; server:
// checked again); onBlockKey on the server. hit is the looked-at block and the face looked at.
public interface IBlockKeyTool
{
    enum BlockKey { PICKUP, PLACE }

    boolean usesBlockKey(ItemStack stack, Level level, BlockHitResult hit, Player player, BlockKey key);

    void onBlockKey(ItemStack stack, ServerPlayer player, BlockHitResult hit, BlockKey key);
}
