package com.mowmaster.mowlib.storage;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.access.ItemAccess;

// Lets an item lying in the world be used like an item in an inventory, so its fluid/energy capabilities can be
// emptied (e.g. a magnet pulling the fluid out of a bucket on the ground). The work is TwoSlotItemAccess's (the item
// copied into a 2-slot holder); after a committed transaction, writeBack puts the results back into the world.
public class ItemEntityAccess
{
    private final ItemEntity entity;
    private final TwoSlotItemAccess holder;

    public ItemEntityAccess(ItemEntity entity)
    {
        this.entity = entity;
        this.holder = new TwoSlotItemAccess(entity.getItem());
    }

    // Pass this to an item capability (ItemAccess.getCapability or a type's getItemHandler)
    public ItemAccess access()
    {
        return holder.access();
    }

    // The entity gets the item as it is now; anything extra is dropped next to it
    public void writeBack(ServerLevel level)
    {
        ItemStack main = holder.result();
        if (main.isEmpty()) entity.discard();
        else entity.setItem(main);

        ItemStack extra = holder.extra();
        if (!extra.isEmpty())
        {
            ItemEntity dropped = new ItemEntity(level, entity.getX(), entity.getY(), entity.getZ(), extra);
            dropped.setDeltaMovement(0, 0, 0);
            level.addFreshEntity(dropped);
        }
    }
}
