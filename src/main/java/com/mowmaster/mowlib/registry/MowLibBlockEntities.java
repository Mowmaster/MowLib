package com.mowmaster.mowlib.registry;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.storage.CBBulkStorageBlock;
import com.mowmaster.mowlib.storage.CBBulkStorageBlockEntity;
import com.mowmaster.mowlib.tint.CBTintBlockEntity;
import com.mowmaster.mowlib.tint.IColorableBlock;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Predicate;
import java.util.function.Supplier;

// Block entity types shared by every mod using MowLib. Their valid blocks are found across ALL mods' blocks:
// block entity types are registered after every mod's blocks, so the scan sees them all.
public class MowLibBlockEntities
{
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, MowLibReferences.MODID);

    // Plain colourable blocks (those that don't bring their own block entity)
    public static final Supplier<BlockEntityType<CBTintBlockEntity>> TINT_BLOCK_ENTITY = BLOCK_ENTITIES.register("tint_block_entity",
            () -> new BlockEntityType<>(CBTintBlockEntity::new,
                    blocksMatching(block -> block instanceof IColorableBlock colorable && colorable.usesTintBlockEntity())));

    // Every CBBulkStorageBlock (ingot piles, log piles, ...)
    public static final Supplier<BlockEntityType<CBBulkStorageBlockEntity>> BULK_STORAGE = BLOCK_ENTITIES.register("bulk_storage",
            () -> new BlockEntityType<>(CBBulkStorageBlockEntity::new, blocksMatching(block -> block instanceof CBBulkStorageBlock)));

    private static Block[] blocksMatching(Predicate<Block> test)
    {
        return BuiltInRegistries.BLOCK.stream().filter(test).toArray(Block[]::new);
    }

    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITIES.register(eventBus);
    }
}
