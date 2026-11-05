package com.mowmaster.mowlib.reading;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;

import java.util.List;

// The server's part of a block's reading paper (see ReadingPaper.fromServer): lines worked out on the server and sent
// to the reading player twice a second while they look at it. On the block entity or the block. At most
// ReadingPayloads.MAX_LINES lines are sent.
public interface IServerReadable
{
    List<Component> readOnServer(ServerLevel level, BlockPos pos, Player player);
}
