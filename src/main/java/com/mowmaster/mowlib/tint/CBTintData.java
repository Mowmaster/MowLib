package com.mowmaster.mowlib.tint;

import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.Arrays;

// Per-region colour storage for a block entity. CBTintBlockEntity keeps its colours in one of these; block entities
// that can't extend CBTintBlockEntity (they already extend a vanilla container class: chests, barrels, piles) own one
// too and forward to it:
// - getColor / setColor / getTint          -> the ITintHolder methods
// - saveAdditional / loadAdditional         -> save / load (keys "color", "color1", ...)
// - getUpdateTag                            -> updateTag() (colours only, never the stored items)
// - applyImplicitComponents                 -> applyItemColor (the placed item's dyed_color)
// - collectImplicitComponents               -> collectItemColor (region 0's colour onto the item)
public class CBTintData
{
    private static final String TAG_COLOR = "color";

    private final BlockEntity owner;
    private final int[] colors;

    public CBTintData(BlockEntity owner, int regions)
    {
        this.owner = owner;
        this.colors = new int[regions];
        Arrays.fill(colors, CBTintBlockEntity.UNDYED);
    }

    public int regionCount()
    {
        return colors.length;
    }

    public boolean isValidRegion(int region)
    {
        return region >= 0 && region < colors.length;
    }

    public int getColor(int region)
    {
        return isValidRegion(region) ? colors[region] : CBTintBlockEntity.UNDYED;
    }

    public void setColor(int region, int color)
    {
        if (!isValidRegion(region)) return;
        colors[region] = color;
        owner.setChanged(); // marks the chunk as needing to be saved
        Level level = owner.getLevel();
        if (level != null)
        {
            // Sends the owner's getUpdatePacket() to nearby clients
            level.sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), Block.UPDATE_CLIENTS);
        }
    }

    // What the renderer asks for: the block decides which region each model tintindex reads from.
    // Undyed regions use the block's default colour for that region (white unless the block says otherwise).
    public int getTint(int tintIndex)
    {
        BlockState state = owner.getBlockState();
        int region = state.getBlock() instanceof IColorableBlock colorable ? colorable.getRegionForTintIndex(state, tintIndex) : tintIndex;
        int color = getColor(region);
        if (color != CBTintBlockEntity.UNDYED) return color;
        return state.getBlock() instanceof IColorableBlock colorable ? colorable.getDefaultColor(region) : CBTintBlockEntity.DEFAULT_TINT;
    }

    // Region 0 keeps the original key so existing saves still load; region N saves as "colorN"
    private static String key(int region)
    {
        return region == 0 ? TAG_COLOR : TAG_COLOR + region;
    }

    // ---- Saving ----

    public void save(ValueOutput output)
    {
        for (int region = 0; region < colors.length; region++)
        {
            if (colors[region] != CBTintBlockEntity.UNDYED) output.putInt(key(region), colors[region]);
        }
    }

    public void load(ValueInput input)
    {
        for (int region = 0; region < colors.length; region++)
        {
            colors[region] = input.getIntOr(key(region), CBTintBlockEntity.UNDYED);
        }
        // On the client the block state didn't change, so force the chunk to re-mesh with the new colours
        Level level = owner.getLevel();
        if (level != null && level.isClientSide())
        {
            level.sendBlockUpdated(owner.getBlockPos(), owner.getBlockState(), owner.getBlockState(), Block.UPDATE_ALL);
        }
    }

    // What the client is sent: only the colours (never the stored items)
    public CompoundTag updateTag()
    {
        CompoundTag tag = new CompoundTag();
        for (int region = 0; region < colors.length; region++)
        {
            if (colors[region] != CBTintBlockEntity.UNDYED) tag.putInt(key(region), colors[region]);
        }
        return tag;
    }

    // ---- Item <-> block (data components) ----

    // The placed item's dyed_color goes on `region`. Set directly, without setChanged or a client update: this runs
    // while the block is being placed, and the placing code saves and syncs the block entity afterwards.
    public void applyItemColor(DataComponentGetter components, int region)
    {
        DyedItemColor dyed = components.get(DataComponents.DYED_COLOR);
        if (dyed != null && isValidRegion(region)) colors[region] = dyed.rgb();
    }

    // Block turned into an item (creative pick-block, or a block that keeps its data when broken): region 0's colour
    public void collectItemColor(DataComponentMap.Builder components)
    {
        int color = getColor(0);
        if (color != CBTintBlockEntity.UNDYED) components.set(DataComponents.DYED_COLOR, new DyedItemColor(color));
    }
}
