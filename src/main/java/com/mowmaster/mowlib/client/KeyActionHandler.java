package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.keys.IActivatable;
import com.mowmaster.mowlib.keys.IBlockKeyTool;
import com.mowmaster.mowlib.keys.IPickupableBlock;
import com.mowmaster.mowlib.keys.KeyPayloads;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;

import java.util.Optional;

// What the action keys do (MowLibKeyMappings: activate, place, pick up), for every mod's items and blocks. Client only:
// it works out what's hovered, held or looked at and asks the server (keys/KeyPayloads), which checks and acts.
// Since 26.3 key codes are SDL scancodes (InputConstants.KEY_*), and screens pass keys as a KeyEvent.
@EventBusSubscriber(modid = MowLibReferences.MODID, value = Dist.CLIENT)
public final class KeyActionHandler
{
    private KeyActionHandler() {}

    // ---- In inventories: activate the hovered item ----

    @SubscribeEvent
    public static void onScreenKey(ScreenEvent.KeyPressed.Pre event)
    {
        if (!MowLibKeyMappings.ACTIVATE.isActiveAndMatches(InputConstants.getKey(event.getKeyEvent()))) return;
        Slot slot = ClientLook.hoveredSlot(event.getScreen());
        if (slot == null || !canActivate(slot.getItem())) return;
        ClientPacketDistributor.sendToServer(new KeyPayloads.Activate(slot.index, Optional.empty()));
        event.setCanceled(true); // the key did its job; nothing else should use it
    }

    // ---- In the world ----

    // Key presses outside screens are counted and read once a tick (consumeClick is true once per press)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        while (MowLibKeyMappings.ACTIVATE.consumeClick()) activateHeld();
        while (MowLibKeyMappings.PLACE.consumeClick()) placeHeld();
        while (MowLibKeyMappings.PICKUP.consumeClick()) pickUpLookedAt();
    }

    // The held IActivatable (main hand first), with the looked-at block for items that want it
    private static void activateHeld()
    {
        Player player = Minecraft.getInstance().player;
        if (player == null) return;
        for (InteractionHand hand : InteractionHand.values())
        {
            ItemStack stack = player.getItemInHand(hand);
            // Something the key does on the client (the Blank Filter's radial menu) comes first
            if (ClientActivations.activate(stack, hand)) return;
            if (!canActivate(stack)) continue;
            BlockHitResult hit = ClientLook.lookedAtBlock();
            Optional<BlockPos> at = hit != null && ((IActivatable) stack.getItem()).usesBlockActivation(stack)
                    ? Optional.of(hit.getBlockPos()) : Optional.empty();
            ClientPacketDistributor.sendToServer(new KeyPayloads.Activate(hand == InteractionHand.MAIN_HAND ? KeyPayloads.MAIN_HAND : KeyPayloads.OFF_HAND, at));
            return;
        }
    }

    // A held IBlockKeyTool that wants this key at the looked-at block (a filter adding/removing it) first; otherwise
    // the held IKeyPlaceable block item (main hand first) against the looked-at face
    private static void placeHeld()
    {
        Player player = Minecraft.getInstance().player;
        BlockHitResult hit = ClientLook.lookedAtBlock();
        if (player == null || hit == null) return;
        if (useBlockKeyTool(player, hit, IBlockKeyTool.BlockKey.PLACE)) return;
        boolean offhand;
        if (KeyPayloads.Place.isPlaceable(player.getMainHandItem())) offhand = false;
        else if (KeyPayloads.Place.isPlaceable(player.getOffhandItem())) offhand = true;
        else return;
        BlockPos pos = hit.getBlockPos();
        Vec3 at = hit.getLocation();
        ClientPacketDistributor.sendToServer(new KeyPayloads.Place(offhand, pos, hit.getDirection(),
                (float) (at.x - pos.getX()), (float) (at.y - pos.getY()), (float) (at.z - pos.getZ())));
    }

    // A held IBlockKeyTool that wants this key at the looked-at block first; otherwise the looked-at IPickupableBlock
    private static void pickUpLookedAt()
    {
        Minecraft minecraft = Minecraft.getInstance();
        BlockHitResult hit = ClientLook.lookedAtBlock();
        if (minecraft.level == null || minecraft.player == null || hit == null) return;
        if (useBlockKeyTool(minecraft.player, hit, IBlockKeyTool.BlockKey.PICKUP)) return;
        if (!(minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof IPickupableBlock)) return;
        ClientPacketDistributor.sendToServer(new KeyPayloads.Pickup(hit.getBlockPos()));
    }

    // Asks the server to run a held IBlockKeyTool (main hand first) that wants this key here. true if one does.
    private static boolean useBlockKeyTool(Player player, BlockHitResult hit, IBlockKeyTool.BlockKey key)
    {
        for (InteractionHand hand : InteractionHand.values())
        {
            ItemStack held = player.getItemInHand(hand);
            if (held.getItem() instanceof IBlockKeyTool tool && tool.usesBlockKey(held, player.level(), hit, player, key))
            {
                // ordinal(): the key's position in the enum, which is all the message needs to carry
                ClientPacketDistributor.sendToServer(new KeyPayloads.BlockKeyTool(hand == InteractionHand.OFF_HAND,
                        hit.getBlockPos(), hit.getDirection(), key.ordinal()));
                return true;
            }
        }
        return false;
    }

    private static boolean canActivate(ItemStack stack)
    {
        return stack.getItem() instanceof IActivatable activatable && activatable.canActivate(stack);
    }
}
