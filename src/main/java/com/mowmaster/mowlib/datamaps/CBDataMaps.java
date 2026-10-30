package com.mowmaster.mowlib.datamaps;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.registries.datamaps.DataMapType;
import net.neoforged.neoforge.registries.datamaps.RegisterDataMapTypesEvent;

@EventBusSubscriber(modid = MowLibReferences.MODID)
public class CBDataMaps
{
    // data/<namespace>/data_maps/block/block_colors.json
    public static final DataMapType<Block, CBBlockColor> BLOCK_COLORS = DataMapType.builder(
                    Identifier.fromNamespaceAndPath(MowLibReferences.MODID, "block_colors"), Registries.BLOCK, CBBlockColor.CODEC)
            // Sent to clients that have the mod; not required, so vanilla-compatible clients can still join
            .synced(CBBlockColor.CODEC, false)
            .build();

    @SubscribeEvent
    public static void register(RegisterDataMapTypesEvent event)
    {
        event.register(BLOCK_COLORS);
    }
}
