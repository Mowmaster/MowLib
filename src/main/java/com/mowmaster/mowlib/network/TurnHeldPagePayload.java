package com.mowmaster.mowlib.network;

import com.mowmaster.mowlib.MowLibIds;
import com.mowmaster.mowlib.paging.IPageable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client -> server: "turn the page of the IPageable item in my main hand (or offhand) forward or back" (sent when a
// page key is pressed in the world while holding one). NEW in MowLib.
public record TurnHeldPagePayload(boolean offhand, int direction) implements CustomPacketPayload
{
    public static final Type<TurnHeldPagePayload> TYPE = new Type<>(MowLibIds.id("turn_held_page"));

    public static final StreamCodec<ByteBuf, TurnHeldPagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, TurnHeldPagePayload::offhand,
            ByteBufCodecs.VAR_INT, TurnHeldPagePayload::direction,
            TurnHeldPagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    public static void handle(TurnHeldPagePayload payload, IPayloadContext context)
    {
        Player player = context.player();
        ItemStack stack = player.getItemInHand(payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
        // Integer.signum turns any number into -1, 0 or 1, so a client can't skip pages
        int direction = Integer.signum(payload.direction());
        if (direction != 0 && stack.getItem() instanceof IPageable pageable)
        {
            pageable.setPage(stack, pageable.nextPage(stack, direction));
        }
    }
}
