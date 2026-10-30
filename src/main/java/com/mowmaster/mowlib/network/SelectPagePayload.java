package com.mowmaster.mowlib.network;

import com.mowmaster.mowlib.MowLibIds;
import com.mowmaster.mowlib.paging.IPageable;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

// Client -> server: "the IPageable item in this slot of my open container is now at this page" (sent when the player
// scrolls over it or presses a page key over it). Vanilla bundles send ServerboundSelectBundleItemPacket the same
// way; that one only knows about bundles. (Moved from Bibliomania's SelectBookPagePayload.)
// A payload is a record holding the data, a Type (its id) and a StreamCodec (how to write and read it).
public record SelectPagePayload(int slot, int page) implements CustomPacketPayload
{
    public static final Type<SelectPagePayload> TYPE = new Type<>(MowLibIds.id("select_page"));

    // Writes slot then page as variable-length ints, and reads them back in the same order
    public static final StreamCodec<ByteBuf, SelectPagePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, SelectPagePayload::slot,
            ByteBufCodecs.VAR_INT, SelectPagePayload::page,
            SelectPagePayload::new);

    @Override
    public Type<? extends CustomPacketPayload> type()
    {
        return TYPE;
    }

    // Runs on the server's main thread. The client can send anything, so check the slot and item first.
    public static void handle(SelectPagePayload payload, IPayloadContext context)
    {
        Player player = context.player();
        AbstractContainerMenu menu = player.containerMenu;
        if (payload.slot() < 0 || payload.slot() >= menu.slots.size()) return;

        ItemStack stack = menu.slots.get(payload.slot()).getItem();
        if (stack.getItem() instanceof IPageable pageable && stack.getCount() == 1) pageable.setPage(stack, payload.page());
    }
}
