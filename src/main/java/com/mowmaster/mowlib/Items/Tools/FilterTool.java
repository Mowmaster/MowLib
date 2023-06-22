package com.mowmaster.mowlib.Items.Tools;

import com.mowmaster.mowlib.BlockEntities.MowLibBaseBlockEntity;
import com.mowmaster.mowlib.BlockEntities.MowLibBaseFilterableBlockEntity;
import com.mowmaster.mowlib.MowLibUtils.MowLibMessageUtils;
import com.mowmaster.mowlib.Registry.DeferredRegisterItems;
import com.mowmaster.mowlib.api.Tools.IMowLibTool;
import com.mowmaster.mowlib.api.TransportAndStorage.IFilterItem;
import net.minecraft.ChatFormatting;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class FilterTool extends BaseTool implements IMowLibTool
{
    public FilterTool(Properties p_41383_) {
        super(p_41383_.stacksTo(1));
    }

    @Override
    public ItemStack getMainTool() { return DeferredRegisterItems.TOOL_FILTERTOOL.get().getDefaultInstance(); }

    @Override
    public ItemStack getSwappedTool() { return DeferredRegisterItems.TOOL_WORKTOOL.get().getDefaultInstance(); }

    @Override
    public void getBlockEntityDetailFilterable(MowLibBaseFilterableBlockEntity baseFilterableBlockEntity, Player player) {
        if(baseFilterableBlockEntity.hasFilter())
        {
            ItemStack filterInPedestal = baseFilterableBlockEntity.getFilterInBlockEntity();
            if(filterInPedestal.getItem() instanceof IFilterItem filter)
            {
                filter.chatDetails(player,baseFilterableBlockEntity,filterInPedestal);
            }
        }
        else
        {
            MowLibMessageUtils.messagePlayerChat(player,ChatFormatting.LIGHT_PURPLE,MODID + ".tool_filter_missing");
        }
    }
}
