package com.mowmaster.mowlib.network;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.paging.IPageHolder;
import com.mowmaster.mowlib.paging.PageArrows;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client -> server: "turn the page of the IPageHolder block entity I'm looking at, forward or back" (sent when a page
// key is pressed in the world while looking at one). NEW in MowLib.
public record TurnBlockPagePayload(BlockPos pos, int direction) implements CustomPacketPayload
{
    public static final Type<TurnBlockPagePayload> TYPE = new Type<>(MowLibReferences.id("turn_block_page"));

    // How close the player must be (8 blocks, squared) for the server to accept it
    private static final double MAX_DISTANCE_SQR = 64.0;

    public static final StreamCodec<ByteBuf, TurnBlockPagePayload> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC, TurnBlockPagePayload::pos,
            ByteBufCodecs.VAR_INT, TurnBlockPagePayload::direction,
            TurnBlockPagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(TurnBlockPagePayload payload, IPayloadContext context)
    {
        Player player = context.player();
        Level level = player.level();
        BlockPos pos = payload.pos();
        int direction = Integer.signum(payload.direction());
        // Never trust the client: the block must be loaded and within reach
        if (direction == 0 || !level.isLoaded(pos)) return;
        if (player.position().distanceToSqr(Vec3.atCenterOf(pos)) > MAX_DISTANCE_SQR) return;

        if (level.getBlockEntity(pos) instanceof IPageHolder holder)
        {
            holder.turnPage(direction);
            PageArrows.playTurnSound(level, pos);
        }
    }
}
