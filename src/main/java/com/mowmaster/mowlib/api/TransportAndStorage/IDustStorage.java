package com.mowmaster.mowlib.api.TransportAndStorage;

import com.mowmaster.mowlib.Capabilities.Dust.DustMagic;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.NotNull;

public interface IDustStorage
{
    /*
     * This method will allow items once emptied to be removed from the world or inventories.
     *
     * @param stack Stack if item being emptied
     * @return TRUE if the itemstack can be removed if its empty.
     *
     */
    boolean consumedOnEmpty(ItemStack dustItemStack);
}
