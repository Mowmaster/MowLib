package com.mowmaster.mowlib.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

// A block entity whose saved data is also what clients see (renderers, HUDs). Extend it instead of BlockEntity to get:
// - sync(): marks it changed (so it's saved) and sends its data to the clients watching it
// - getUpdateTag / getUpdatePacket: the client copy is everything saveAdditional writes (saveCustomOnly: no position or
//   type id), sent when the chunk loads and on every sync()
// Override getUpdateTag yourself if some of what's saved should stay on the server.
public abstract class CBSyncedBlockEntity extends BlockEntity
{
    protected CBSyncedBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state)
    {
        super(type, pos, state);
    }

    // Saved and sent to clients. Safe to call before the block entity is in a level (it's then only marked changed).
    public void sync()
    {
        setChanged();
        if (level != null) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return saveCustomOnly(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
