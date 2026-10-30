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

import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;

// Block entity types shared by every mod using MowLib. Their valid blocks are found across ALL mods' blocks:
// block entity types are registered after every mod's blocks, so the scan sees them all.
//
// MowLib itself has no blocks, so on its own (and in MowLib's datagen run) both sets are empty. That's allowed:
// NeoForge's BlockEntityType(factory, Block...) constructor throws on an EMPTY varargs list ("instantiated without
// valid blocks"), but the BlockEntityType(factory, Set<Block>) constructor accepts an empty set on purpose. So these
// pass a Set. No example blocks are needed to keep MowLib loading.
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

    // Every registered block (from any mod) that passes the test, as a Set (which may be empty, see above).
    // Collectors.toUnmodifiableSet() gathers a stream into a Set that can't be changed afterwards.
    private static Set<Block> blocksMatching(Predicate<Block> test)
    {
        return BuiltInRegistries.BLOCK.stream().filter(test).collect(Collectors.toUnmodifiableSet());
    }

    public static void register(IEventBus eventBus)
    {
        BLOCK_ENTITIES.register(eventBus);
    }
}
