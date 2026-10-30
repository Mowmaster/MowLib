package com.mowmaster.mowlib.registry;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.FilterData;
import com.mowmaster.mowlib.paging.PagedContents;
import com.mowmaster.mowlib.palette.ColorPaletteContents;
import com.mowmaster.mowlib.transport.TransportContents;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.block.Block;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

// MowLib's item data components (replace 1.20's stack NBT). persistent = saved with the item,
// networkSynchronized = sent to clients (needed for tooltips and item models).
public class MowLibDataComponents
{
    public static final DeferredRegister.DataComponents COMPONENTS =
            DeferredRegister.createDataComponents(Registries.DATA_COMPONENT_TYPE, MowLibReferences.MODID);

    // field: what a paged storage item holds (pages, selected page, each page's item and amount)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<PagedContents>> PAGED_CONTENTS =
            COMPONENTS.registerComponentType("paged_contents", builder -> builder.persistent(PagedContents.CODEC).networkSynchronized(PagedContents.STREAM_CODEC));

    // The transport type a filter is currently set to (its "mode"). Separate from FILTER_DATA so the item model
    // can switch texture on it (items/<filter>.json uses a "minecraft:component" select). Loading turns an old
    // (aliased) type id into the type's current one, so the model's cases ("mowlib:items"...) match it.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Identifier>> FILTER_MODE =
            COMPONENTS.registerComponentType("filter_mode", builder -> builder.persistent(Identifier.CODEC.xmap(MowLibDataComponents::currentTypeId, id -> id))
                    .networkSynchronized(Identifier.STREAM_CODEC));

    public static final DeferredHolder<DataComponentType<?>, DataComponentType<FilterData>> FILTER_DATA =
            COMPONENTS.registerComponentType("filter_data", builder -> builder.persistent(FilterData.CODEC).networkSynchronized(FilterData.STREAM_CODEC));

    // A work card's selected positions
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<List<BlockPos>>> WORK_POSITIONS =
            COMPONENTS.registerComponentType("work_positions", builder -> builder.persistent(BlockPos.CODEC.listOf())
                    .networkSynchronized(BlockPos.STREAM_CODEC.apply(ByteBufCodecs.list())));

    // The contents of a storage carried as an item (any mod's "packet", "tank item", ...)
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<TransportContents<?>>> TRANSPORT_CONTENTS =
            COMPONENTS.registerComponentType("transport_contents", builder -> builder.persistent(TransportContents.CODEC).networkSynchronized(TransportContents.STREAM_CODEC));

    // The block a colour picker took its colour from, so the Custom Dye crafted from it can be named after it
    // ("Diamond Block Dye"). Saved as the block's id; ByteBufCodecs.registry sends it as the registry's number.
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<Block>> PICKED_BLOCK =
            COMPONENTS.registerComponentType("picked_block", builder -> builder.persistent(BuiltInRegistries.BLOCK.byNameCodec())
                    .networkSynchronized(ByteBufCodecs.registry(Registries.BLOCK)));

    // A Color Palette's dyes (sorted by colour) and which one is selected
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<ColorPaletteContents>> COLOR_PALETTE =
            COMPONENTS.registerComponentType("color_palette", builder -> builder.persistent(ColorPaletteContents.CODEC)
                    .networkSynchronized(ColorPaletteContents.STREAM_CODEC));

    private static Identifier currentTypeId(Identifier id)
    {
        TransportType<?> type = TransportTypes.REGISTRY.getValue(id);
        return type != null ? type.getId() : id;
    }

    public static void register(IEventBus eventBus)
    {
        COMPONENTS.register(eventBus);
    }
}
