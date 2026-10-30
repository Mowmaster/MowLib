package com.mowmaster.mowlib.tint;

import com.mowmaster.mowlib.registry.MowLibBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

// The block entity of every plain IColorableBlock (mowlib:tint_block_entity). Its colours live in a CBTintData,
// the same class storage block entities use, so saving, syncing and the item colour work the same everywhere.
public class CBTintBlockEntity extends BlockEntity implements ITintHolder
{
    // Colours are stored as 0xRRGGBB with no alpha, so -1 can never be a real colour
    public static final int UNDYED = -1;
    // Colour shown for an undyed region. White leaves the greyscale texture as-is.
    public static final int DEFAULT_TINT = 0xFFFFFF;

    // One colour per tint region. Single-colour blocks only have region 0.
    private final CBTintData tints;
    // The region most recently added by a state change (e.g. second slab placed). Not saved: it only lives
    // between the state change and applyImplicitComponents during a single placement.
    private int addedRegion = IColorableBlock.NO_REGION;

    public CBTintBlockEntity(BlockPos pos, BlockState state)
    {
        super(MowLibBlockEntities.TINT_BLOCK_ENTITY.get(), pos, state);
        int regions = state.getBlock() instanceof IColorableBlock colorable ? colorable.getTintRegionCount() : 1;
        this.tints = new CBTintData(this, regions);
    }

    @Override
    public int getColor(int region)
    {
        return tints.getColor(region);
    }

    @Override
    public void setColor(int region, int color)
    {
        tints.setColor(region, color);
    }

    // What the renderer asks for: the block decides which region each model tintindex reads from
    @Override
    public int getTint(int tintIndex)
    {
        return tints.getTint(tintIndex);
    }

    // Vanilla calls this when the block state changes but the block stays the same, so this block entity is kept
    @Override
    public void setBlockState(BlockState newState)
    {
        BlockState oldState = getBlockState();
        super.setBlockState(newState);
        if (newState.getBlock() instanceof IColorableBlock colorable)
        {
            int added = colorable.getAddedRegion(oldState, newState);
            if (added != IColorableBlock.NO_REGION) addedRegion = added;
        }
    }

    // Returns the region added by the last placement, and clears it so it's only used once
    public int takeAddedRegion()
    {
        int added = addedRegion;
        addedRegion = IColorableBlock.NO_REGION;
        return added;
    }

    // ---- Saving to disk ----

    @Override
    protected void saveAdditional(ValueOutput output)
    {
        super.saveAdditional(output);
        tints.save(output);
    }

    @Override
    protected void loadAdditional(ValueInput input)
    {
        super.loadAdditional(input);
        tints.load(input); // also re-meshes the chunk on the client
    }

    // ---- Syncing to the client ----

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries)
    {
        return tints.updateTag();
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket()
    {
        return ClientboundBlockEntityDataPacket.create(this); // uses getUpdateTag
    }

    // ---- Item <-> block (data components) ----

    // Item placed: if it carries a dyed_color, apply it to the region that was placed.
    // A second slab colours the half that was just added; a fresh block colours its own region.
    // Never clears an existing colour, so placing a second slab can't wipe the first half.
    @Override
    protected void applyImplicitComponents(DataComponentGetter components)
    {
        super.applyImplicitComponents(components);
        BlockState state = getBlockState();
        int region;
        if (addedRegion != IColorableBlock.NO_REGION)
        {
            region = addedRegion;
        }
        else
        {
            region = state.getBlock() instanceof IColorableBlock colorable
                    ? colorable.getRegionForTintIndex(state, 0)
                    : 0;
        }
        tints.applyItemColor(components, region);
    }

    // Block turned into an item (creative pick-block): region 0's colour goes on the item
    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder components)
    {
        super.collectImplicitComponents(components);
        tints.collectItemColor(components);
    }
}
