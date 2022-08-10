package com.mowmaster.mowlib.Items;

import com.mowmaster.mowlib.api.IDustStorage;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import net.minecraft.world.item.Item.Properties;

public class BaseDustStorageBlockItem extends BlockItem implements IDustStorage {
    public BaseDustStorageBlockItem(Block p_40565_, Properties p_40566_) {
        super(p_40565_, p_40566_);
    }

    @Override
    public boolean consumedOnEmpty(ItemStack dustItemStack) {
        return false;
    }
}
