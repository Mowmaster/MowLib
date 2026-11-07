package com.mowmaster.mowlib.link;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

// A linkable's outgoing links (the blocks it sends to) and the rules for changing them:
// - a pair links one way only: adding A -> B removes B -> A (both ends update)
// - a link must be in the sender's range (ILinkable#isInLinkRange) and the sender must have room (getMaxLinks)
// - flip turns a link round, if the new sender can take it
// Every change calls the host's onLinksChanged (save + send to clients). Saved under "links" as a list of positions.
// Order is kept (the first link is the first tried; a round robin can start anywhere).
public class LinkList
{
    private static final String KEY = "links";

    private final ILinkable host;
    private final List<BlockPos> links = new ArrayList<>();

    public LinkList(ILinkable host)
    {
        this.host = host;
    }

    // ---- Reading ----

    public List<BlockPos> get()
    {
        return List.copyOf(links);
    }

    public BlockPos get(int index)
    {
        return links.get(index);
    }

    public int size()
    {
        return links.size();
    }

    public boolean isEmpty()
    {
        return links.isEmpty();
    }

    public boolean contains(BlockPos target)
    {
        return links.contains(target);
    }

    // ---- Changing ----

    // Adds a link to target (if it's already there: ADDED, nothing changes). A link back from target is removed.
    public LinkResult add(BlockPos target)
    {
        BlockPos pos = host.getBlockPos();
        if (target.equals(pos)) return LinkResult.SELF;
        if (links.contains(target)) return LinkResult.ADDED;
        if (!host.isInLinkRange(target)) return LinkResult.OUT_OF_RANGE;
        if (links.size() >= host.getMaxLinks()) return LinkResult.FULL;
        links.add(target.immutable());
        host.onLinksChanged();
        Level level = host.getLevel();
        if (level != null && level.getBlockEntity(target) instanceof ILinkable other) other.getLinkList().remove(pos);
        return LinkResult.ADDED;
    }

    // false if it wasn't linked
    public boolean remove(BlockPos target)
    {
        if (!links.remove(target)) return false;
        host.onLinksChanged();
        return true;
    }

    // Removes several at once (one onLinksChanged), e.g. links to blocks that are gone
    public void removeAll(Collection<BlockPos> targets)
    {
        if (links.removeAll(targets)) host.onLinksChanged();
    }

    // Adds a link, or removes it if it's there (the linking tool)
    public LinkResult toggle(BlockPos target)
    {
        if (target.equals(host.getBlockPos())) return LinkResult.SELF;
        if (remove(target)) return LinkResult.REMOVED;
        return add(target);
    }

    // Turns the link between the host and other round, whichever way it goes now. Both ends update. If the new
    // sender can't take it (out of its range, no room), nothing changes and the result says why.
    public LinkResult flip(BlockPos other)
    {
        Level level = host.getLevel();
        if (level == null || !(level.getBlockEntity(other) instanceof ILinkable otherLinkable)) return LinkResult.NOT_LINKABLE;
        BlockPos pos = host.getBlockPos();
        ILinkable sender = links.contains(other) ? host : otherLinkable.getLinkList().contains(pos) ? otherLinkable : null;
        if (sender == null) return LinkResult.NOT_LINKABLE;
        ILinkable receiver = sender == host ? otherLinkable : host;
        sender.getLinkList().remove(receiver.getBlockPos());
        LinkResult result = receiver.getLinkList().add(sender.getBlockPos());
        if (!result.changed())
        {
            sender.getLinkList().links.add(receiver.getBlockPos().immutable()); // put it back as it was
            sender.onLinksChanged();
        }
        return result;
    }

    // ---- Saving (call from the block entity's saveAdditional / loadAdditional) ----

    public void save(ValueOutput output)
    {
        if (!links.isEmpty()) output.store(KEY, BlockPos.CODEC.listOf(), links);
    }

    public void load(ValueInput input)
    {
        links.clear();
        input.read(KEY, BlockPos.CODEC.listOf()).ifPresent(links::addAll);
    }

    // ---- A linkable was broken ----

    // Every loaded linkable close enough to link to pos stops sending to it (call from the broken block entity's
    // preRemoveSideEffects, on the server). range: the largest link range any linkable that could reach pos has.
    // Linkables in unloaded chunks keep the dead link until they notice (e.g. the next time they try to send).
    // Block entities are kept per chunk, so this looks through the chunks within range.
    public static void removeLinksTo(ServerLevel level, BlockPos pos, int range)
    {
        int chunks = (range >> 4) + 1;
        int centreX = pos.getX() >> 4, centreZ = pos.getZ() >> 4;
        for (int chunkX = centreX - chunks; chunkX <= centreX + chunks; chunkX++)
        {
            for (int chunkZ = centreZ - chunks; chunkZ <= centreZ + chunks; chunkZ++)
            {
                if (!level.hasChunk(chunkX, chunkZ)) continue;
                for (BlockEntity blockEntity : level.getChunk(chunkX, chunkZ).getBlockEntities().values())
                {
                    if (blockEntity instanceof ILinkable sender && !blockEntity.getBlockPos().equals(pos)) sender.getLinkList().remove(pos);
                }
            }
        }
    }
}
