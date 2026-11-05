package com.mowmaster.mowlib.storage;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

// Where the `other` item of an IInventoryUsable click is, so it can be used up or swapped for a result in place:
// - CURSOR: on the cursor (clicked onto the usable item in `slot`); `carried` is the cursor
// - SLOT: in `slot` (the usable item was on the cursor); the slot may be empty (IInventoryUsable#usedOnEmptySlots)
// - HAND: in the player's hand (a block standing in for the item, e.g. a lectern showing it: InventoryClicks.useHeld)
public record InventoryClick(Kind kind, @Nullable Slot slot, @Nullable SlotAccess carried, @Nullable InteractionHand hand)
{
    public enum Kind { CURSOR, SLOT, HAND }

    public static InventoryClick cursor(Slot slot, SlotAccess carried) { return new InventoryClick(Kind.CURSOR, slot, carried, null); }
    public static InventoryClick slot(Slot slot) { return new InventoryClick(Kind.SLOT, slot, null, null); }
    public static InventoryClick hand(InteractionHand hand) { return new InventoryClick(Kind.HAND, null, null, hand); }

    // Takes up to `count` of `other` (to store it in the usable item). From a slot this goes through Slot#safeTake,
    // which respects slots that can't be taken from (crafting results...) and then gives back EMPTY; elsewhere it
    // splits `other` directly. Returns what was taken.
    public ItemStack take(ItemStack other, int count, Player player)
    {
        if (kind == Kind.SLOT && slot != null) return slot.safeTake(other.getCount(), count, player);
        return other.split(count);
    }

    // Puts something taken out of the usable item where `other` was: onto an empty cursor, or into the (empty or
    // matching) slot. Returns what didn't fit, for the caller to put back. HAND gives it to the player.
    public ItemStack putTaken(ItemStack stack, Player player)
    {
        switch (kind)
        {
            case CURSOR ->
            {
                if (carried != null && carried.get().isEmpty())
                {
                    carried.set(stack);
                    return ItemStack.EMPTY;
                }
                return stack;
            }
            case SLOT ->
            {
                // safeInsert: only what the slot accepts (mayPlace) goes in; the rest comes back
                return slot != null ? slot.safeInsert(stack) : stack;
            }
            default ->
            {
                StorageInteractions.giveToPlayer(player, stack);
                return ItemStack.EMPTY;
            }
        }
    }

    // One of `other` is used up and becomes `result` (an empty bucket filled...): a stack of one is replaced where it
    // is; from a bigger stack one is taken and the result goes to the player's inventory. Server.
    public void exchangeOne(ItemStack other, ItemStack result, Player player)
    {
        if (other.getCount() == 1) replace(result, player);
        else
        {
            other.shrink(1);
            StorageInteractions.giveToPlayer(player, result);
        }
        changed();
    }

    // Puts `stack` where `other` was (it must be used up, or empty: an empty cursor taking something out). Server.
    public void replace(ItemStack stack, Player player)
    {
        switch (kind)
        {
            case CURSOR -> { if (carried != null) carried.set(stack); }
            case SLOT -> { if (slot != null) slot.set(stack); }
            case HAND -> { if (hand != null) player.setItemInHand(hand, stack); }
        }
    }

    // Something taken out of the usable item: onto an empty cursor if it was a cursor click, else to the inventory
    public void giveTaken(ItemStack stack, Player player)
    {
        if (kind == Kind.CURSOR && carried != null && carried.get().isEmpty()) carried.set(stack);
        else StorageInteractions.giveToPlayer(player, stack);
    }

    // `other` was changed in place (shrunk...): tells its slot, so the change is saved and sent
    public void changed()
    {
        if (slot != null) slot.setChanged();
    }
}
