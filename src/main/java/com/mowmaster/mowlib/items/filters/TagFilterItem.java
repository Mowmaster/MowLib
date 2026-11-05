package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.filter.FilterData;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.Optional;

// The tag filters (ported from 1.20's FilterTag / FilterTagMachine): match anything in the selected tag of any Tag
// Getter on the list. Items mode checks item tags, fluids mode fluid tags; other modes follow the plain
// whitelist/blacklist rule. Only Tag Getters go on the list (click them onto the filter, see BaseFilterItem; set each
// getter's tag first, see TagGetterItem).
// Two items share this class: mowlib:filter_tag (INSERT: what goes into the holder) and mowlib:filter_tag_machine
// (NEUTRAL: what a machine works on, e.g. a block breaker).
public class TagFilterItem extends BaseFilterItem
{
    public TagFilterItem(Properties properties, FilterDirection direction)
    {
        super(properties, direction);
    }

    // Items and fluids are the types with tags to match
    @Override
    protected boolean handlesType(TransportType<?> type)
    {
        return type == TransportTypes.ITEMS.get() || type == TransportTypes.FLUIDS.get();
    }

    @Override
    protected boolean usesListFor(TransportType<?> type)
    {
        return handlesType(type);
    }

    // Only Tag Getters that hold a tag
    @Override
    protected boolean acceptsListEntry(ItemStack filter, ItemStack entry)
    {
        return TagGetterItem.selectedTag(entry).isPresent();
    }

    // A Tag Getter on the list is shown by its selected tag ("#c:ingots"), which is what the filter matches, not by
    // its item name
    @Override
    protected Component describeListEntry(ItemStack entry)
    {
        Optional<Identifier> tag = TagGetterItem.selectedTag(entry);
        return tag.isPresent() ? Component.literal("#" + tag.get()) : super.describeListEntry(entry);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, TransportType<R> type, R resource, FilterData.Entry entry)
    {
        for (ItemStackTemplate template : entry.list())
        {
            Optional<Identifier> tag = TagGetterItem.selectedTag(template.create());
            if (tag.isPresent() && inTag(resource, tag.get())) return true;
        }
        return false;
    }

    // TagKey.create: the tag with this id in the item (or block, or fluid) registry. typeHolder: the item's registry
    // entry; a fluid's state can be asked about its tags directly. An item that places a block also matches its block's
    // tags, so a getter that read a block's tags (mineable/pickaxe...) works on items too.
    private static boolean inTag(Resource resource, Identifier tag)
    {
        if (resource instanceof ItemResource item)
        {
            if (item.typeHolder().is(TagKey.create(Registries.ITEM, tag))) return true;
            return item.getItem() instanceof BlockItem blockItem && blockItem.getBlock().builtInRegistryHolder().is(TagKey.create(Registries.BLOCK, tag));
        }
        if (resource instanceof FluidResource fluid) return fluid.getFluid().defaultFluidState().is(TagKey.create(Registries.FLUID, tag));
        return false;
    }
}
