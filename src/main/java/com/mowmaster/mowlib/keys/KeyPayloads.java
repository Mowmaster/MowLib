package com.mowmaster.mowlib.keys;

import com.mowmaster.mowlib.MowLibReferences;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.Optional;

// The key actions' messages (client -> server), sent by client/KeyActionHandler. The server never trusts them: every
// handler checks the item is really there, and the block is within the player's reach and theirs to change.
public final class KeyPayloads
{
    private KeyPayloads() {}

    // Hands in a slot number: an open container's slots are 0 and up
    public static final int MAIN_HAND = -1;
    public static final int OFF_HAND = -2;

    // ---- Activate: the item in a slot (or hand), optionally at the looked-at block ----

    // ByteBufCodecs.optional: an Optional written as a present flag then the value
    public record Activate(int slot, Optional<BlockPos> at) implements CustomPacketPayload
    {
        public static final Type<Activate> TYPE = new Type<>(MowLibReferences.id("activate"));
        public static final StreamCodec<ByteBuf, Activate> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.VAR_INT, Activate::slot,
                ByteBufCodecs.optional(BlockPos.STREAM_CODEC), Activate::at,
                Activate::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static void handle(Activate payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player)) return;
            ItemStack stack;
            if (payload.slot() == MAIN_HAND) stack = player.getItemInHand(InteractionHand.MAIN_HAND);
            else if (payload.slot() == OFF_HAND) stack = player.getItemInHand(InteractionHand.OFF_HAND);
            else
            {
                AbstractContainerMenu menu = player.containerMenu;
                if (payload.slot() < 0 || payload.slot() >= menu.slots.size()) return;
                stack = menu.slots.get(payload.slot()).getItem();
            }
            if (!(stack.getItem() instanceof IActivatable activatable) || !activatable.canActivate(stack)) return;
            if (payload.at().isPresent() && payload.slot() < 0)
            {
                BlockPos pos = payload.at().get();
                if (player.isWithinBlockInteractionRange(pos, 1.0) && activatable.activateAtBlock(stack, player, pos)) return;
            }
            activatable.activate(stack, player);
        }
    }

    // ---- Place: the held IKeyPlaceable block item against a block face ----

    // The hit position as offsets inside the clicked block (0 to 1), which is all placing needs
    public record Place(boolean offhand, BlockPos pos, Direction face, float x, float y, float z) implements CustomPacketPayload
    {
        public static final Type<Place> TYPE = new Type<>(MowLibReferences.id("place_held"));
        public static final StreamCodec<ByteBuf, Place> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, Place::offhand,
                BlockPos.STREAM_CODEC, Place::pos,
                Direction.STREAM_CODEC, Place::face,
                ByteBufCodecs.FLOAT, Place::x,
                ByteBufCodecs.FLOAT, Place::y,
                ByteBufCodecs.FLOAT, Place::z,
                Place::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static boolean isPlaceable(ItemStack stack)
        {
            return stack.getItem() instanceof BlockItem && stack.getItem() instanceof IKeyPlaceable placeable && placeable.canPlaceByKey(stack);
        }

        public static void handle(Place payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player)) return;
            InteractionHand hand = payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND;
            ItemStack stack = player.getItemInHand(hand);
            if (!isPlaceable(stack) || !player.isWithinBlockInteractionRange(payload.pos(), 1.0)) return;
            Vec3 hit = Vec3.atLowerCornerOf(payload.pos()).add(clamp(payload.x()), clamp(payload.y()), clamp(payload.z()));
            BlockHitResult hitResult = new BlockHitResult(hit, payload.face(), payload.pos(), false);
            BlockItem item = (BlockItem) stack.getItem();
            // 26.3: a swing says how it's swung; read before placing, as placing can use the stack up
            var swing = stack.getInteractAnimation();
            // BlockItem#place: the normal placing of a block item (sound, block entity data, using up the item)
            if (item.place(new BlockPlaceContext(player.level(), player, hand, stack, hitResult)).consumesAction())
            {
                // true: send the swing to clients (the placing player's included: the key press didn't swing locally)
                player.swing(hand, swing, true);
            }
        }

        private static double clamp(float offset)
        {
            return Math.clamp(offset, 0f, 1f);
        }
    }

    // ---- Pick up: the looked-at IPickupableBlock ----

    public record Pickup(BlockPos pos) implements CustomPacketPayload
    {
        public static final Type<Pickup> TYPE = new Type<>(MowLibReferences.id("pickup_block"));
        public static final StreamCodec<ByteBuf, Pickup> STREAM_CODEC = BlockPos.STREAM_CODEC.map(Pickup::new, Pickup::pos);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static void handle(Pickup payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
            if (!player.isWithinBlockInteractionRange(payload.pos(), 1.0) || !level.mayInteract(player, payload.pos())) return;
            if (IPickupableBlock.pickUpAt(level, payload.pos(), player))
            {
                player.swing(InteractionHand.MAIN_HAND, player.getMainHandItem().getInteractAnimation(), true);
            }
        }
    }

    // ---- Page keys with an IPageKeyTool held, at a block ----

    public record PageKeyTool(boolean offhand, BlockPos pos, int direction) implements CustomPacketPayload
    {
        public static final Type<PageKeyTool> TYPE = new Type<>(MowLibReferences.id("page_key_tool"));
        public static final StreamCodec<ByteBuf, PageKeyTool> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, PageKeyTool::offhand,
                BlockPos.STREAM_CODEC, PageKeyTool::pos,
                ByteBufCodecs.VAR_INT, PageKeyTool::direction,
                PageKeyTool::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static void handle(PageKeyTool payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player) || !(player.level() instanceof ServerLevel level)) return;
            ItemStack stack = player.getItemInHand(payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            int direction = Integer.signum(payload.direction());
            if (direction == 0 || !(stack.getItem() instanceof IPageKeyTool tool)) return;
            if (!player.isWithinBlockInteractionRange(payload.pos(), 1.0) || !level.mayInteract(player, payload.pos())) return;
            if (tool.usesPageKeys(stack, level, payload.pos(), player)) tool.onPageKey(stack, player, payload.pos(), direction);
        }
    }

    // ---- Pick-up / place keys with an IBlockKeyTool held, at a block ----

    // key: the IBlockKeyTool.BlockKey's position in its enum (0 pick up, 1 place); face: the side looked at
    public record BlockKeyTool(boolean offhand, BlockPos pos, Direction face, int key) implements CustomPacketPayload
    {
        public static final Type<BlockKeyTool> TYPE = new Type<>(MowLibReferences.id("block_key_tool"));
        public static final StreamCodec<ByteBuf, BlockKeyTool> STREAM_CODEC = StreamCodec.composite(
                ByteBufCodecs.BOOL, BlockKeyTool::offhand,
                BlockPos.STREAM_CODEC, BlockKeyTool::pos,
                Direction.STREAM_CODEC, BlockKeyTool::face,
                ByteBufCodecs.VAR_INT, BlockKeyTool::key,
                BlockKeyTool::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static void handle(BlockKeyTool payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player)) return;
            IBlockKeyTool.BlockKey[] keys = IBlockKeyTool.BlockKey.values();
            if (payload.key() < 0 || payload.key() >= keys.length) return;
            ItemStack stack = player.getItemInHand(payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!(stack.getItem() instanceof IBlockKeyTool tool)) return;
            // Only within reach. (Nothing in the world is changed by these tools themselves, so no protection check here;
            // a tool that does change the block must check level.mayInteract itself.)
            if (!player.isWithinBlockInteractionRange(payload.pos(), 1.0)) return;
            BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(payload.pos()), payload.face(), payload.pos(), false);
            IBlockKeyTool.BlockKey key = keys[payload.key()];
            if (tool.usesBlockKey(stack, player.level(), hit, player, key)) tool.onBlockKey(stack, player, hit, key);
        }
    }
}
