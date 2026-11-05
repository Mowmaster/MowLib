package com.mowmaster.mowlib.keys;

import com.mowmaster.mowlib.registry.MowLibDataComponents;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;

// An item that does something when the activation key (MowLibKeyMappings.ACTIVATE, default V) is pressed:
// - over it in an inventory (it's hovered), or held (main hand first): activate()
// - held while looking at a block: activateAtBlock() first (an Atlas marking the block); if that says false, activate()
// The server does the work (keys/KeyPayloads.Activate); these run on the server only.
// The common case is an on/off mode kept in mowlib:activated: implement canActivate and call toggle() from activate().
// Tooltips can name the key with Component.keybind(MowLibKeyMappings.ACTIVATE_NAME).
public interface IActivatable
{
    // Whether this stack reacts to the key at all (checked on the client before asking the server, and again there)
    boolean canActivate(ItemStack stack);

    void activate(ItemStack stack, ServerPlayer player);

    // Held and looking at a block. true: handled (activate() isn't called)
    default boolean activateAtBlock(ItemStack stack, ServerPlayer player, BlockPos pos)
    {
        return false;
    }

    // Whether the client should send the looked-at block with the key press (only items that use activateAtBlock)
    default boolean usesBlockActivation(ItemStack stack)
    {
        return false;
    }

    // ---- The on/off mode (mowlib:activated) ----

    static boolean isActive(ItemStack stack)
    {
        return stack.getOrDefault(MowLibDataComponents.ACTIVATED.get(), false);
    }

    // Flips it, with a beacon sound and a message over the hotbar (onKey / offKey: lang keys taking the item's name;
    // the two-argument version uses MowLib's "<name> activated" / "<name> deactivated"). Returns the new state.
    static boolean toggle(ItemStack stack, ServerPlayer player, String onKey, String offKey)
    {
        boolean active = !isActive(stack);
        stack.set(MowLibDataComponents.ACTIVATED.get(), active);
        player.level().playSound(null, player.blockPosition(), active ? SoundEvents.BEACON_ACTIVATE : SoundEvents.BEACON_DEACTIVATE,
                SoundSource.PLAYERS, 0.4f, 1.6f);
        player.sendOverlayMessage(Component.translatable(active ? onKey : offKey, stack.getHoverName()));
        return active;
    }

    static boolean toggle(ItemStack stack, ServerPlayer player)
    {
        return toggle(stack, player, "message.mowlib.activated", "message.mowlib.deactivated");
    }
}
