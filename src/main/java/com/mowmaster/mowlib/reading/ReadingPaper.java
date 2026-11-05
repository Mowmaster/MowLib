package com.mowmaster.mowlib.reading;

import com.mowmaster.mowlib.paging.PagedContents;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.List;

// What goes on the reading HUD's paper (the big panel, by default on the left):
// - lines: text, in order (the first is usually the name). Colours too pale to read on paper are darkened.
// - pages: a paged storage's pages, drawn as a grid of slots under the text (the open page ringed), or null
// - askServer: also ask the server (IServerReadable on the block) and add its lines under these; for what only the
//   server knows (a network's totals, another block's contents...)
// A record: an immutable holder whose constructor, getters, equals and toString Java writes for us.
public record ReadingPaper(List<Component> lines, @Nullable PagedContents pages, boolean askServer)
{
    public ReadingPaper
    {
        lines = List.copyOf(lines);
    }

    public static ReadingPaper of(List<Component> lines)
    {
        return new ReadingPaper(lines, null, false);
    }

    public static ReadingPaper withPages(List<Component> lines, @Nullable PagedContents pages)
    {
        return new ReadingPaper(lines, pages, false);
    }

    // These lines (often just the block's name), then the server's
    public static ReadingPaper fromServer(List<Component> lines)
    {
        return new ReadingPaper(lines, null, true);
    }
}
