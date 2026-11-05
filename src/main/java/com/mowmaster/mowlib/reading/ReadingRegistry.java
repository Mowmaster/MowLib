package com.mowmaster.mowlib.reading;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Reading HUD hooks for things you can't implement an interface on (vanilla blocks, other mods' blocks and mobs).
// Register during mod setup; they're asked in the order added.
// - block readers (client): a paper for a looked-at block that isn't an IReadableBlock
// - label lines (server): extra lines for the small label of a block or mob (after MowLib's own: stored energy,
//   fluids and experience; a chiseled bookshelf's book; a mob's effects and growing up)
public final class ReadingRegistry
{
    private ReadingRegistry() {}

    @FunctionalInterface
    public interface BlockReader
    {
        @Nullable ReadingPaper read(ReadingContext context);
    }

    @FunctionalInterface
    public interface BlockLabelLines
    {
        void addLines(ServerLevel level, BlockPos pos, Player player, List<Component> lines);
    }

    @FunctionalInterface
    public interface EntityLabelLines
    {
        void addLines(Entity entity, Player player, List<Component> lines);
    }

    private static final List<BlockReader> BLOCK_READERS = new CopyOnWriteArrayList<>();
    private static final List<BlockLabelLines> BLOCK_LABELS = new CopyOnWriteArrayList<>();
    private static final List<EntityLabelLines> ENTITY_LABELS = new CopyOnWriteArrayList<>();

    public static void addBlockReader(BlockReader reader) { BLOCK_READERS.add(reader); }
    public static void addBlockLabelLines(BlockLabelLines lines) { BLOCK_LABELS.add(lines); }
    public static void addEntityLabelLines(EntityLabelLines lines) { ENTITY_LABELS.add(lines); }

    // The paper for a looked-at block: its block entity's, its block's, then the registered readers'
    public static @Nullable ReadingPaper read(ReadingContext context)
    {
        if (context.blockEntity() instanceof IReadableBlock readable)
        {
            ReadingPaper paper = readable.read(context);
            if (paper != null) return paper;
        }
        if (context.state().getBlock() instanceof IReadableBlock readable)
        {
            ReadingPaper paper = readable.read(context);
            if (paper != null) return paper;
        }
        for (BlockReader reader : BLOCK_READERS)
        {
            ReadingPaper paper = reader.read(context);
            if (paper != null) return paper;
        }
        return null;
    }

    static void addBlockLabelLines(ServerLevel level, BlockPos pos, Player player, List<Component> lines)
    {
        for (BlockLabelLines each : BLOCK_LABELS) each.addLines(level, pos, player, lines);
    }

    static void addEntityLabelLines(Entity entity, Player player, List<Component> lines)
    {
        for (EntityLabelLines each : ENTITY_LABELS) each.addLines(entity, player, lines);
    }
}
