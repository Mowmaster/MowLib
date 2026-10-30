package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.network.SelectPagePayload;
import com.mowmaster.mowlib.network.TurnBlockPagePayload;
import com.mowmaster.mowlib.network.TurnHeldPagePayload;
import com.mowmaster.mowlib.paging.IPageHolder;
import com.mowmaster.mowlib.paging.IPageable;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import org.jetbrains.annotations.Nullable;

// Page turning for every IPageable item and IPageHolder block entity, from any mod:
// - In an inventory, over an IPageable item: scroll (down = next, like the bundle) or press a page key.
// - In the world: a page key turns the looked-at IPageHolder block, or else the held IPageable item (main hand first).
// The change is made here straight away (so tooltips update) and the server is told.
// Vanilla scrolls bundles with an ItemSlotMouseAction, but only the screen itself can add those, so we listen for the
// scroll event instead. (Scrolling moved from Bibliomania's BookScrollHandler; the keys are NEW.)
@EventBusSubscriber(modid = MowLibReferences.MODID, value = Dist.CLIENT)
public final class PageInputHandler
{
    private PageInputHandler() {}

    // ---- In inventories ----

    @SubscribeEvent
    public static void onScroll(ScreenEvent.MouseScrolled.Pre event)
    {
        if (event.getScrollDeltaY() == 0) return;
        if (selectOnHoveredSlot(event.getScreen(), event.getScrollDeltaY() < 0 ? 1 : -1))
        {
            event.setCanceled(true); // don't also scroll the screen (e.g. the creative tab list)
        }
    }

    @SubscribeEvent
    public static void onKey(ScreenEvent.KeyPressed.Pre event)
    {
        // 26.x passes keys as a KeyEvent; InputConstants.getKey turns it into the Key a KeyMapping compares with.
        // isActiveAndMatches (NeoForge) also checks the key's conflict context and modifiers.
        InputConstants.Key key = InputConstants.getKey(event.getKeyEvent());
        int direction = MowLibKeyMappings.PAGE_NEXT.isActiveAndMatches(key) ? 1
                : MowLibKeyMappings.PAGE_PREVIOUS.isActiveAndMatches(key) ? -1 : 0;
        if (direction != 0 && selectOnHoveredSlot(event.getScreen(), direction)) event.setCanceled(true);
    }

    // Returns true if the mouse was over a paged item (so the event was used)
    private static boolean selectOnHoveredSlot(Screen screen, int direction)
    {
        // <?> is a wildcard: any container screen, whatever menu type it has
        if (!(screen instanceof AbstractContainerScreen<?> containerScreen)) return false;
        Slot slot = containerScreen.getHoveredSlot(); // public getter added by NeoForge
        if (slot == null) return false;
        ItemStack stack = slot.getItem();
        if (!(stack.getItem() instanceof IPageable pageable) || stack.getCount() != 1) return false;

        int page = pageable.nextPage(stack, direction);
        pageable.setPage(stack, page);
        ClientPacketDistributor.sendToServer(new SelectPagePayload(slot.index, page));
        return true;
    }

    // ---- In the world ----

    // Key presses outside screens are counted up and read here once per tick (consumeClick returns true once per press)
    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event)
    {
        while (MowLibKeyMappings.PAGE_NEXT.consumeClick()) turnInWorld(1);
        while (MowLibKeyMappings.PAGE_PREVIOUS.consumeClick()) turnInWorld(-1);
    }

    private static void turnInWorld(int direction)
    {
        Minecraft minecraft = Minecraft.getInstance();
        Player player = minecraft.player;
        if (player == null || minecraft.level == null) return;

        // The looked-at block first
        BlockHitResult hit = lookedAtBlock(minecraft);
        if (hit != null && minecraft.level.getBlockEntity(hit.getBlockPos()) instanceof IPageHolder)
        {
            ClientPacketDistributor.sendToServer(new TurnBlockPagePayload(hit.getBlockPos(), direction));
            return;
        }
        // Otherwise a held paged item
        for (InteractionHand hand : InteractionHand.values())
        {
            ItemStack stack = player.getItemInHand(hand);
            if (stack.getItem() instanceof IPageable pageable)
            {
                pageable.setPage(stack, pageable.nextPage(stack, direction));
                ClientPacketDistributor.sendToServer(new TurnHeldPagePayload(hand == InteractionHand.OFF_HAND, direction));
                return;
            }
        }
    }

    @Nullable
    private static BlockHitResult lookedAtBlock(Minecraft minecraft)
    {
        // hitResult is what the crosshair is on, worked out each frame
        HitResult hitResult = minecraft.hitResult;
        if (hitResult instanceof BlockHitResult blockHit && hitResult.getType() == HitResult.Type.BLOCK) return blockHit;
        return null;
    }
}
