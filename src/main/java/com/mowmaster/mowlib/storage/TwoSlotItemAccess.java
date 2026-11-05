package com.mowmaster.mowlib.storage;

import net.minecraft.core.NonNullList;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemUtil;

// An item that isn't in a normal inventory slot (lying in the world, in another container's slot...) made usable
// as an ItemAccess, which is what NeoForge's item capabilities need, so a bucket can become an empty bucket "in place".
// The stack is copied into a small 2-slot holder: slot 0 is the item itself, slot 1 catches anything that can't go
// back into slot 0 (one empty bucket out of a stack of full ones...). After the action, the caller puts result()
// back where the item came from and does something with extra() (drop it, give it to the player).
// Used by ItemEntityAccess (items in the world) and StorageInteractions.withSlotAccess (other containers' slots).
public final class TwoSlotItemAccess
{
    private final ItemStacksResourceHandler handler;

    public TwoSlotItemAccess(ItemStack stack)
    {
        // NonNullList.of(empty value, elements...): a fixed-size list that never holds null
        this.handler = new ItemStacksResourceHandler(NonNullList.of(ItemStack.EMPTY, stack.copy(), ItemStack.EMPTY));
    }

    // Pass this to an item capability (ItemAccess.getCapability or a type's getItemHandler)
    public ItemAccess access()
    {
        return ItemAccess.forHandlerIndex(handler, 0);
    }

    // What the item is now (EMPTY if it was used up)
    public ItemStack result()
    {
        return ItemUtil.getStack(handler, 0);
    }

    // Anything extra that came out of it (EMPTY if nothing)
    public ItemStack extra()
    {
        return ItemUtil.getStack(handler, 1);
    }
}
