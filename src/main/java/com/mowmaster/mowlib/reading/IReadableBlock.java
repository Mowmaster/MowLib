package com.mowmaster.mowlib.reading;

import org.jetbrains.annotations.Nullable;

// A block that puts something on the reading HUD's paper while looked at. Implement it on the block entity or on the
// block (the block entity is asked first). Called on the client once a tick, with what the client knows (synced
// block entity data); return null for "nothing on the paper" (the HUD then shows the small label instead). For
// things only the server knows, return ReadingPaper.fromServer(...) and implement IServerReadable too.
// ReadingPapers has the common pieces (an item's tooltip, a paged item with its page grid...).
public interface IReadableBlock
{
    @Nullable ReadingPaper read(ReadingContext context);
}
