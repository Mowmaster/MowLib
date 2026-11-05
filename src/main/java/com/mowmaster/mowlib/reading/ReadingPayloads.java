package com.mowmaster.mowlib.reading;

import com.mowmaster.mowlib.MowLibReferences;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentSerialization;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// The reading HUD asks the server what it can't know on the client (registered in MowLibNetwork):
// - BlockRequest (client -> server): about the block at pos; paper = true for an IServerReadable's paper lines, false
//   for the small label's (ReadingLabels; slot: the chiseled bookshelf slot looked at, or -1)
// - BlockAnswer (server -> client): those lines
// - EntityRequest / EntityAnswer: a mob's label lines, by entity id
// Asked twice a second while looking. Only answered for players who can read (ReadingSight), within 64 blocks.
public final class ReadingPayloads
{
    private ReadingPayloads() {}

    public static final int MAX_LINES = 16;
    private static final double MAX_DISTANCE_SQR = 64 * 64;

    public record BlockRequest(BlockPos pos, int slot, boolean paper) implements CustomPacketPayload
    {
        public static final Type<BlockRequest> TYPE = new Type<>(MowLibReferences.id("reading_block_request"));
        public static final StreamCodec<ByteBuf, BlockRequest> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BlockRequest::pos,
                ByteBufCodecs.VAR_INT, BlockRequest::slot,
                ByteBufCodecs.BOOL, BlockRequest::paper,
                BlockRequest::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }
    }

    // ComponentSerialization.STREAM_CODEC sends text with its styles; .apply(ByteBufCodecs.list(n)): a list of at most n
    public record BlockAnswer(BlockPos pos, int slot, boolean paper, List<Component> lines) implements CustomPacketPayload
    {
        public static final Type<BlockAnswer> TYPE = new Type<>(MowLibReferences.id("reading_block"));
        public static final StreamCodec<RegistryFriendlyByteBuf, BlockAnswer> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, BlockAnswer::pos,
                ByteBufCodecs.VAR_INT, BlockAnswer::slot,
                ByteBufCodecs.BOOL, BlockAnswer::paper,
                ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINES)), BlockAnswer::lines,
                BlockAnswer::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }
    }

    public record EntityRequest(int id) implements CustomPacketPayload
    {
        public static final Type<EntityRequest> TYPE = new Type<>(MowLibReferences.id("reading_entity_request"));
        public static final StreamCodec<ByteBuf, EntityRequest> STREAM_CODEC = ByteBufCodecs.VAR_INT.map(EntityRequest::new, EntityRequest::id);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }
    }

    public record EntityAnswer(int id, List<Component> lines) implements CustomPacketPayload
    {
        public static final Type<EntityAnswer> TYPE = new Type<>(MowLibReferences.id("reading_entity"));
        public static final StreamCodec<RegistryFriendlyByteBuf, EntityAnswer> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, EntityAnswer::id,
                ComponentSerialization.STREAM_CODEC.apply(ByteBufCodecs.list(MAX_LINES)), EntityAnswer::lines,
                EntityAnswer::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }
    }

    // ---- Server ----

    public static void handleBlockRequest(BlockRequest request, IPayloadContext context)
    {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel level) || !ReadingSight.canRead(player)) return;
        BlockPos pos = request.pos();
        if (!level.isLoaded(pos) || player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > MAX_DISTANCE_SQR) return;
        List<Component> lines;
        if (request.paper())
        {
            IServerReadable readable = level.getBlockEntity(pos) instanceof IServerReadable fromEntity ? fromEntity
                    : level.getBlockState(pos).getBlock() instanceof IServerReadable fromBlock ? fromBlock : null;
            if (readable == null) return;
            lines = readable.readOnServer(level, pos, player);
            if (lines.size() > MAX_LINES) lines = lines.subList(0, MAX_LINES);
        }
        else lines = ReadingLabels.describeBlock(level, pos, request.slot(), player);
        context.reply(new BlockAnswer(pos, request.slot(), request.paper(), lines));
    }

    public static void handleEntityRequest(EntityRequest request, IPayloadContext context)
    {
        Player player = context.player();
        if (!(player.level() instanceof ServerLevel level) || !ReadingSight.canRead(player)) return;
        Entity entity = level.getEntity(request.id());
        if (entity == null || player.distanceToSqr(entity) > MAX_DISTANCE_SQR) return;
        context.reply(new EntityAnswer(request.id(), ReadingLabels.describeEntity(entity, player)));
    }

    // ---- Client: the last answers, read by client/reading/ReadingHud ----
    // Plain fields (no client classes here, so this class also loads on dedicated servers). Handlers run on the main
    // thread, so there's no threading to worry about.

    public static @Nullable BlockPos paperPos;
    public static List<Component> paperLines = List.of();
    public static @Nullable BlockPos labelPos;
    public static int labelSlot = -1;
    public static List<Component> labelLines = List.of();
    public static int entityId = -1;
    public static List<Component> entityLines = List.of();

    public static void handleBlockAnswer(BlockAnswer answer, IPayloadContext context)
    {
        if (answer.paper())
        {
            paperPos = answer.pos();
            paperLines = answer.lines();
        }
        else
        {
            labelPos = answer.pos();
            labelSlot = answer.slot();
            labelLines = answer.lines();
        }
    }

    public static void handleEntityAnswer(EntityAnswer answer, IPayloadContext context)
    {
        entityId = answer.id();
        entityLines = answer.lines();
    }
}
