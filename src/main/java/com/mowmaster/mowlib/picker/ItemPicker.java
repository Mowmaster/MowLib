package com.mowmaster.mowlib.picker;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.storage.StorageInteractions;
import com.mowmaster.mowlib.workcard.IWorkCard;
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
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;

// Picker groups (PickerGroup): blank items turned into one of a set by picking from a radial menu. For every mod:
//   public static final PickerGroup UPGRADES = ItemPicker.register(new PickerGroup(
//           Identifier.fromNamespaceAndPath("mymod", "upgrades"), MyItems.BLANK_UPGRADE, item -> item instanceof MyUpgrade, true, true));
// plus a lang entry "picker.mymod.upgrades": "Pick an upgrade", and a PickerBaseItem (or any item) as the blank.
// Opening the menu, with a blank held in `hand`:
// - MowLib's activation key, for groups with activationKey (set up by MowLibClient)
// - from server code (an item's use(), a block's click...): ItemPicker.open(serverPlayer, group, hand)
// - from client code (your own key, a button...): ItemPicker.openOnClient(group, hand)
// Picking sends Pick; the server checks the blank is still in that hand and the item is one of the group's, then swaps
// one blank for it.
public final class ItemPicker
{
    private ItemPicker() {}

    // CopyOnWriteArrayList: safe to add to during setup while the game reads it
    private static final List<PickerGroup> GROUPS = new CopyOnWriteArrayList<>();

    public static PickerGroup register(PickerGroup group)
    {
        GROUPS.add(group);
        return group;
    }

    // MowLib's own groups. Static fields: registered the first time anything uses this class.
    public static final PickerGroup FILTERS = register(new PickerGroup(MowLibReferences.id("filters"),
            MowLibItems.FILTER_BASE, item -> item instanceof ITransportFilter, true, true));
    public static final PickerGroup WORK_CARDS = register(new PickerGroup(MowLibReferences.id("work_cards"),
            MowLibItems.WORKCARD_BASE, item -> item instanceof IWorkCard, true, true));

    public static Optional<PickerGroup> get(Identifier id)
    {
        return GROUPS.stream().filter(group -> group.id().equals(id)).findFirst();
    }

    // The group this stack is the blank of
    public static Optional<PickerGroup> forBase(ItemStack stack)
    {
        return GROUPS.stream().filter(group -> group.isBase(stack)).findFirst();
    }

    // The group with a reset recipe that this item is a choice of (recipes/PickerResetRecipe)
    public static Optional<PickerGroup> forReset(Item item)
    {
        return GROUPS.stream().filter(group -> group.resetRecipe() && group.isChoice().test(item)).findFirst();
    }

    // ---- Opening the menu ----

    // Server code: opens the menu on that player's screen
    public static void open(ServerPlayer player, PickerGroup group, InteractionHand hand)
    {
        PacketDistributor.sendToPlayer(player, new Open(group.id(), hand == InteractionHand.OFF_HAND));
    }

    // What opens the menu on the client. MowLibClient sets it (to client/ItemPickerScreen.open); a plain function here
    // keeps this common class free of client classes, so it also loads on dedicated servers.
    private static volatile BiConsumer<PickerGroup, InteractionHand> clientOpener = (group, hand) -> {};

    public static void setClientOpener(BiConsumer<PickerGroup, InteractionHand> opener)
    {
        clientOpener = opener;
    }

    // Client code: opens the menu
    public static void openOnClient(PickerGroup group, InteractionHand hand)
    {
        clientOpener.accept(group, hand);
    }

    // ---- Messages (registered in MowLibNetwork) ----

    // Client -> server: "swap one blank of this group in this hand for this item"
    public record Pick(Identifier group, boolean offhand, Identifier choice) implements CustomPacketPayload
    {
        public static final Type<Pick> TYPE = new Type<>(MowLibReferences.id("picker_pick"));
        public static final StreamCodec<ByteBuf, Pick> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Pick::group,
                ByteBufCodecs.BOOL, Pick::offhand,
                Identifier.STREAM_CODEC, Pick::choice,
                Pick::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        public static void handle(Pick payload, IPayloadContext context)
        {
            if (!(context.player() instanceof ServerPlayer player)) return;
            Optional<PickerGroup> found = get(payload.group());
            if (found.isEmpty()) return;
            PickerGroup group = found.get();
            ItemStack blank = player.getItemInHand(payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
            if (!group.isBase(blank)) return;
            // getValue gives air for an unknown id, which is never a choice
            Item choice = BuiltInRegistries.ITEM.getValue(payload.choice());
            if (!group.isChoice().test(choice)) return;
            // consume: one used up (creative players keep theirs, as vanilla does)
            blank.consume(1, player);
            StorageInteractions.giveToPlayer(player, new ItemStack(choice));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1f);
        }
    }

    // Server -> client: "open this group's menu" (ItemPicker.open)
    public record Open(Identifier group, boolean offhand) implements CustomPacketPayload
    {
        public static final Type<Open> TYPE = new Type<>(MowLibReferences.id("picker_open"));
        public static final StreamCodec<ByteBuf, Open> STREAM_CODEC = StreamCodec.composite(
                Identifier.STREAM_CODEC, Open::group,
                ByteBufCodecs.BOOL, Open::offhand,
                Open::new);

        @Override
        public Type<? extends CustomPacketPayload> type()
        {
            return TYPE;
        }

        // On the client's main thread
        public static void handle(Open payload, IPayloadContext context)
        {
            get(payload.group()).ifPresent(group -> openOnClient(group, payload.offhand() ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND));
        }
    }
}
