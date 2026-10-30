package com.mowmaster.mowlib;

import com.mojang.logging.LogUtils;
import com.mowmaster.mowlib.client.PagedFullnessProperty;
import com.mowmaster.mowlib.creative.CreativeTabRegistry;
import com.mowmaster.mowlib.registry.MowLibBlockEntities;
import com.mowmaster.mowlib.registry.MowLibItems;
import com.mowmaster.mowlib.registry.MowLibRecipes;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;

// Shared code for Mowmaster's mods: tintable blocks (IColorableBlock and friends), the colour picker and custom dye,
// and bulk item storage. Mods depend on it (usually bundled with Jar-in-Jar) and use its classes and registries.
@Mod(MowLibReferences.MODID)
public class MowLib
{
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MowLibReferences.MODID);

    // Creates a new Block with the id "storageslabs:example_block", combining the namespace and path
    public static DeferredBlock<Block> EXAMPLE_BLOCK = BLOCKS.registerBlock("example_block", properties -> new ExampleTintableBlock(properties.sound(SoundType.STONE)));
    public static DeferredBlock<Block> EXAMPLE_STORAGE_BLOCK = BLOCKS.registerBlock("example_storage_block", properties -> new ExampleStorageBlock(properties.sound(SoundType.STONE)));
    // Creates a new BlockItem with the id "storageslabs:example_block", combining the namespace and path
    public static final DeferredItem<BlockItem> EXAMPLE_BLOCK_ITEM = MowLibItems.ITEMS.registerSimpleBlockItem("example_block", EXAMPLE_BLOCK);


    public MowLib(IEventBus modEventBus, ModContainer modContainer)
    {
        MowLibDataComponents.register(modEventBus);
        TransportTypes.register(modEventBus);
        BLOCKS.register(modEventBus);
        MowLibItems.register(modEventBus);
        MowLibBlockEntities.register(modEventBus);
        MowLibRecipes.register(modEventBus);
        CreativeTabRegistry.register(modEventBus);
    }
}
