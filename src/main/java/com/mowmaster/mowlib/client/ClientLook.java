package com.mowmaster.mowlib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.Nullable;

// What the player is pointing at, for client-side key and scroll handlers (PageInputHandler, KeyActionHandler...).
// Client only.
public final class ClientLook
{
    private ClientLook() {}

    // The block under the crosshair, or null (air, a mob, nothing in reach). hitResult is worked out each frame.
    public static @Nullable BlockHitResult lookedAtBlock()
    {
        HitResult hitResult = Minecraft.getInstance().hitResult;
        return hitResult instanceof BlockHitResult hit && hitResult.getType() == HitResult.Type.BLOCK ? hit : null;
    }

    // The inventory slot under the mouse in a container screen, or null (another kind of screen, or no slot there).
    // <?>: any container screen, whatever menu type it has. getHoveredSlot() is a public getter added by NeoForge.
    public static @Nullable Slot hoveredSlot(Screen screen)
    {
        return screen instanceof AbstractContainerScreen<?> containerScreen ? containerScreen.getHoveredSlot() : null;
    }
}
