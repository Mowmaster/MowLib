package com.mowmaster.mowlib.keys;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

// A held item that takes over the page keys (MowLibKeyMappings.PAGE_NEXT / PAGE_PREVIOUS) while looking at certain
// blocks: a tool that sets a value on blocks (a priority, a range, a channel...). Checked before page turning, so the
// keys still turn pages everywhere else. usesPageKeys runs on both sides (client: whether to send; server: checked
// again); onPageKey on the server, with direction +1 (next) or -1 (previous). Sneaking is the player's own to read.
public interface IPageKeyTool
{
    boolean usesPageKeys(ItemStack stack, Level level, BlockPos pos, Player player);

    void onPageKey(ItemStack stack, ServerPlayer player, BlockPos pos, int direction);
}
