package com.mowmaster.mowlib;

import com.mowmaster.mowlib.client.CBBlockEntityTintSource;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.client.ClientPagedContentsTooltip;
import com.mowmaster.mowlib.client.MowLibKeyMappings;
import com.mowmaster.mowlib.client.PagedFullnessProperty;
import com.mowmaster.mowlib.paging.PagedContentsTooltip;
import net.minecraft.client.color.block.BlockTintSource;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.RegisterRangeSelectItemModelPropertyEvent;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterColorHandlersEvent;


import java.util.ArrayList;
import java.util.List;

// Client-only setup. This class never loads on a dedicated server.
@Mod(value = MowLibReferences.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MowLibReferences.MODID, value = Dist.CLIENT)
public class MowLibClient
{
    // Every colourable block from every mod gets one tint source per tint region (region N = model tintindex N).
    // Mods using MowLib must not register their own tint sources for IColorableBlocks.
    @SubscribeEvent
    public static void registerTintSources(RegisterColorHandlersEvent.BlockTintSources event)
    {
        for (Block block : BuiltInRegistries.BLOCK)
        {
            if (block instanceof IColorableBlock colorable)
            {
                List<BlockTintSource> sources = new ArrayList<>();
                for (int region = 0; region < colorable.getTintRegionCount(); region++)
                {
                    sources.add(new CBBlockEntityTintSource(region));
                }
                event.register(sources, block);
            }
        }
    }

    // The page keys (Options > Controls > Key Binds > MowLib). A custom category must be registered as well.
    @SubscribeEvent
    public static void registerKeyMappings(RegisterKeyMappingsEvent event)
    {
        event.registerCategory(MowLibKeyMappings.CATEGORY);
        event.register(MowLibKeyMappings.PAGE_NEXT);
        event.register(MowLibKeyMappings.PAGE_PREVIOUS);
    }

    // Every mod's paged storage items get the page grid tooltip
    @SubscribeEvent
    public static void registerTooltips(RegisterClientTooltipComponentFactoriesEvent event)
    {
        event.register(PagedContentsTooltip.class, ClientPagedContentsTooltip::new);
    }

    // "mowlib:paged_fullness", for item models that fill up (minecraft:range_dispatch)
    @SubscribeEvent
    public static void registerRangeProperties(RegisterRangeSelectItemModelPropertyEvent event)
    {
        event.register(MowLibReferences.id("paged_fullness"), PagedFullnessProperty.MAP_CODEC);
    }
}
