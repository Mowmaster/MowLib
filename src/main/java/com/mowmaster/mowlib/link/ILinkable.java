package com.mowmaster.mowlib.link;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// A block entity that can send to other blocks along links (pedestals...). Links live on the sender only, in its
// LinkList; a pair of linkables links one way at a time (LinkList#add removes the reverse link).
// getBlockPos and getLevel are BlockEntity's own methods, so a block entity implementing this already has them.
// Works with LinkingToolItem (making links), LinkList.removeLinksTo (cleanup when one is broken), and on the client
// with client/LinkBeams and client/LinkOutlines (track the block entity in blockentity/ClientLoadedBlockEntities for
// those to find it).
public interface ILinkable
{
    BlockPos getBlockPos();

    @Nullable Level getLevel();

    LinkList getLinkList();

    // How far (blocks, on each axis: a cube) this can link, and how many links it may have
    int getLinkRange();

    int getMaxLinks();

    // Save and send to clients: called after the links changed
    void onLinksChanged();

    // Where beams start and end on this block (default: its centre)
    default Vec3 getLinkPoint()
    {
        return Vec3.atCenterOf(getBlockPos());
    }

    // The colour (0xRRGGBB) of beams this sends along (default white)
    default int getLinkColor()
    {
        return 0xFFFFFF;
    }

    default boolean isInLinkRange(BlockPos target)
    {
        int range = getLinkRange();
        BlockPos pos = getBlockPos();
        return Math.abs(target.getX() - pos.getX()) <= range
                && Math.abs(target.getY() - pos.getY()) <= range
                && Math.abs(target.getZ() - pos.getZ()) <= range;
    }
}
