package com.mowmaster.mowlib.storage;

import com.mowmaster.mowlib.registry.MowLibBlockEntities;
import com.mowmaster.mowlib.tint.CBTintData;
import com.mowmaster.mowlib.tint.ITintHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.NotNull;

// The block entity for every CBBulkStorageBlock: a CBBulkItemHandler sized by the block, plus its colours.
public class CBBulkStorageBlockEntity extends BlockEntity implements ITintHolder
{
    private final CBTintData tints;
    private final CBBulkItemHandler items;

    public CBBulkStorageBlockEntity(BlockPos pos, BlockState state)
    {
        super(MowLibBlockEntities.BULK_STORAGE.get(), pos, state);
        CBBulkStorageBlock block = (CBBulkStorageBlock) state.getBlock();
        this.tints = new CBTintData(this, block.getTintRegionCount());
        this.items = new CBBulkItemHandler(block.slots(), block.capacity(), block::accepts, this::onItemsChanged);
    }

    public CBBulkItemHandler getItems()
    {
        return items;
    }

    private void onItemsChanged()
    {
        setChanged();
        // Comparators reading this block update
        if (level != null) level.updateNeighbourForOutputSignal(worldPosition, getBlockState().getBlock());
    }

    // ---- Colours ----

    @Override public int getColor(int region) { return tints.getColor(region); }
    @Override public void setColor(int region, int color) { tints.setColor(region, color); }
    @Override public int getTint(int tintIndex) { return tints.getTint(tintIndex); }

    // ---- Broken or replaced: spill the contents as normal stacks ----

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state)
    {
        if (level == null) return;
        ItemStack stack;
        while (!(stack = items.extractStack(Integer.MAX_VALUE)).isEmpty())
        {
            Containers.dropItemStack(level, pos.getX(), pos.getY(), pos.getZ(), stack);
        }
    }

    // ---- Saving ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        tints.save(output);
        items.serialize(output);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        tints.load(input);
        items.deserialize(input);
    }

    // ---- Syncing the colours (only) to clients ----

    @Override
    public @NotNull CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return tints.updateTag();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
