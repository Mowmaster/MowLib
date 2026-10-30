package com.mowmaster.mowlib.drops;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.block.BreakBlockEvent;

// Rolls every matching BonusDrop when a player breaks a block. This uses the block break event rather than a global
// loot modifier because 26.3 rewrote loot conditions (random_chance and others), and this needs no loot JSON.
// (Moved from Bibliomania's GrassFiberHandler.)
@EventBusSubscriber(modid = MowLibReferences.MODID)
public final class BonusDropHandler
{
    private BonusDropHandler() {}

    // LOWEST so that if another mod cancels the break, we never see it (cancelled events skip later listeners)
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onBreak(BreakBlockEvent event)
    {
        // The event fires on both sides; only the server drops items
        if (!(event.getLevel() instanceof ServerLevel level)) return;
        Player player = event.getPlayer();
        if (player.isCreative()) return;

        for (BonusDrop drop : BonusDrops.all())
        {
            if (drop.matches(player, event.getState()) && level.getRandom().nextFloat() < drop.chance())
            {
                ItemStack stack = drop.drop().get();
                if (!stack.isEmpty()) Block.popResource(level, event.getPos(), stack);
            }
        }
    }
}
