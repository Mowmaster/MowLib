package com.mowmaster.mowlib.paging;

import net.minecraft.world.inventory.tooltip.TooltipComponent;

// The data for a paged item's tooltip picture: every page (empty ones too), the selected one, and how much a page
// holds. PagedStorageInteractions#tooltipImage creates it; on the client, ClientPagedContentsTooltip draws it
// (registered by MowLibClient for every mod). This half is common code because items, which servers load, create it.
// (Moved from Bibliomania's BookTooltip.)
public record PagedContentsTooltip(PagedContents contents, int pageCapacity) implements TooltipComponent
{
}
