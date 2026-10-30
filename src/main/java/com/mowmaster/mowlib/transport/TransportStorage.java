package com.mowmaster.mowlib.transport;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.util.ValueIOSerializable;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.ResourceHandlerUtil;
import net.neoforged.neoforge.transfer.resource.Resource;
import org.jetbrains.annotations.Nullable;

// A block entity's (or anything's) storage for one transport type, made by TransportType#createStorage. It's a
// normal NeoForge ResourceHandler, so the same object can be handed to pipes as a capability, plus saving
// (ValueIOSerializable: serialize/deserialize with ValueOutput/ValueInput) and a couple of helpers.
// The built-in storages check their host's filter (TransportStorageHost#filterAllows / #filterLimit) in
// isValid/insert/extract, so filters apply to pipes and hoppers too.
public interface TransportStorage<R extends Resource> extends ResourceHandler<R>, ValueIOSerializable
{
    default boolean isEmpty()
    {
        return ResourceHandlerUtil.isEmpty(this);
    }

    // The holder is being removed and nothing else took the contents: drop what makes sense (items spill,
    // experience becomes orbs). Default: nothing (fluids and energy are lost).
    default void dropContents(Level level, BlockPos pos)
    {
    }

    // One line for an info message ("Water: 1000/16000"), or null when empty
    @Nullable Component describe();
}
