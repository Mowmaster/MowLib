package com.mowmaster.mowlib.filter;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.storage.StorageInteractions;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;

// Turning a Blank Filter into a filter: holding a Blank Filter, the activation key opens a radial menu of every filter
// (client/FilterPickerScreen); clicking one sends Pick, and the server swaps one Blank Filter for that filter.
// "Every filter" means every registered item that is a MowLib filter (ITransportFilter), from any mod.
public final class FilterPicker
{
    private FilterPicker() {}

    // Every filter item, in registry order (MowLib's, then other mods')
    public static List<Item> choices()
    {
        return BuiltInRegistries.ITEM.stream().filter(item -> item instanceof ITransportFilter).toList();
    }

    // Client -> server: "swap one Blank Filter in this hand for this filter". The server checks both.
    public record Pick(boolean offhand, Identifier filter) implements CustomPacketPayload
    {
        public static final Type<Pick> TYPE = new Type<>(MowLibReferences.id("pick_filter"));
        public static final StreamCodec<ByteBuf, Pick> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Pick::offhand,
                Identifier.STREAM_CODEC, Pick::filter,
                Pick::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static void handle(Pick payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack blank = player.getItemInHand(payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!blank.is(MowLibItems.FILTER_BASE.get())) return;
            // getValue gives air for an unknown id, which isn't a filter
            Item filter = BuiltInRegistries.ITEM.getValue(payload.filter());
            if (!(filter instanceof ITransportFilter)) return;
            // consume: one used up (creative players keep theirs, as vanilla does)
            blank.consume(1, player);
            StorageInteractions.giveToPlayer(player, new ItemStack(filter));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
        }
    }
}
