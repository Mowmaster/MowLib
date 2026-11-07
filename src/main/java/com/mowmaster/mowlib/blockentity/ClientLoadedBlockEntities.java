package com.mowmaster.mowlib.blockentity;

import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

// The block entities a mod wants to find on the client without scanning chunks (link beams, "who sends to me" lists).
// A block entity opts in by calling these from its own NeoForge hooks:
//   onLoad()          -> ClientLoadedBlockEntities.loaded(this)
//   onChunkUnloaded() -> ClientLoadedBlockEntities.unloaded(this)
//   setRemoved()      -> ClientLoadedBlockEntities.unloaded(this)
// Only client-side block entities are kept (loaded() ignores server ones), so this is safe to call from common code.
// A set of objects, not positions, so each dimension's block entities are told apart by their level.
public final class ClientLoadedBlockEntities
{
    private ClientLoadedBlockEntities() {}

    private static final Set<BlockEntity> LOADED = ConcurrentHashMap.newKeySet();

    public static void loaded(BlockEntity blockEntity)
    {
        Level level = blockEntity.getLevel();
        if (level != null && level.isClientSide()) LOADED.add(blockEntity);
    }

    public static void unloaded(BlockEntity blockEntity)
    {
        LOADED.remove(blockEntity);
    }

    // The tracked block entities of this class (or interface) in that level
    public static <T> List<T> all(Class<T> type, Level level)
    {
        List<T> found = new ArrayList<>();
        for (BlockEntity blockEntity : LOADED)
        {
            if (blockEntity.getLevel() == level && !blockEntity.isRemoved() && type.isInstance(blockEntity)) found.add(type.cast(blockEntity));
        }
        return found;
    }
}
