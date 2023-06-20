package com.mowmaster.mowlib.Items.Tools;

import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import com.mowmaster.mowlib.api.Tools.IMowLibTool;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class WorkCardTool extends BaseTool implements IMowLibTool
{
    public WorkCardTool(Item.Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_WORKTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_TAGTOOL.get().getDefaultInstance(); }
}
