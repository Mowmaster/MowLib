package com.mowmaster.mowlib.paging;

// A block entity with pages, one of which is open (e.g. a placed storage book). Implement it on the BlockEntity.
// Players can then turn its pages with the page keys while looking at the block (see client/PageInputHandler and
// network/TurnBlockPagePayload); blocks can also call turnPage from their own clicks, e.g. via PageArrows.
// PagedStorage implements the storage side; a block entity using it can simply forward these methods to it.
public interface IPageHolder
{
    int getPageCount();

    // The open page, 0-based
    int getPage();

    // Opens a page and tells clients. Only called on the server.
    void setPage(int page);

    // Turns forward (+1) or back (-1), wrapping round at either end
    default void turnPage(int direction)
    {
        int count = getPageCount();
        if (count > 0) setPage(Math.floorMod(getPage() + direction, count));
    }
}
