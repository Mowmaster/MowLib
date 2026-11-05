package com.mowmaster.mowlib.filter;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.items.filters.BaseFilterItem;
import com.mowmaster.mowlib.items.tools.FilterToolItem;
import com.mowmaster.mowlib.paging.PagedContents;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.storage.StorageInteractions;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// A MowLib filter (any ITransportFilter item) kept inside another item, as the mowlib:held_filter component. The item
// then checks what moves in and out of it with holderAllows. Blocks keep theirs with IFilterHolder instead; a block
// placed from such an item copies the filter across in applyImplicitComponents / collectImplicitComponents.
// Held filters follow the same rules as a block's (ITransportFilter.allowsMoving / limitStored): a filter only acts
// in the direction it's set to. An INSERT filter decides what may go into the item, an EXTRACT filter what may be
// taken out, a NEUTRAL filter what a machine-like item works on; a filter set to another direction lets everything
// through. Only INSERT filters limit how much the item stores.
// Putting a filter in and out: right-click the air holding the item in the main hand, with
// - a filter in the offhand: it goes in (one; not while the item has one)
// - MowLib's Filter Tool in the offhand: it comes out, into the inventory (or "no filter to remove", as for blocks)
// Call useOnHeld from the item's use():
//   InteractionResult result = HeldFilters.useOnHeld(level, player, hand, true);
//   if (result != null) return result;
public final class HeldFilters
{
    private HeldFilters() {}

    // The item's filter (a new stack each time: create() makes one from the template), or EMPTY
    public static ItemStack get(ItemStack holder)
    {
        ItemStackTemplate filter = holder.get(MowLibDataComponents.HELD_FILTER.get());
        return filter == null ? ItemStack.EMPTY : filter.create();
    }

    // The same, for an item seen as a resource (a resource handler's view of a stack)
    public static ItemStack get(ItemResource holder)
    {
        ItemStackTemplate filter = holder.get(MowLibDataComponents.HELD_FILTER.get());
        return filter == null ? ItemStack.EMPTY : filter.create();
    }

    // Puts a filter in (EMPTY takes it out). fromNonEmptyStack: a stack as an immutable template.
    public static void set(ItemStack holder, ItemStack filter)
    {
        if (filter.isEmpty()) holder.remove(MowLibDataComponents.HELD_FILTER.get());
        else holder.set(MowLibDataComponents.HELD_FILTER.get(), ItemStackTemplate.fromNonEmptyStack(filter));
    }

    public static boolean isFilter(ItemStack stack)
    {
        return stack.getItem() instanceof ITransportFilter;
    }

    // Whether a filter lets a resource of a type move this way. No filter, or one set to another direction: allowed.
    public static <R extends Resource> boolean allows(ItemStack filter, TransportType<R> type, R resource, FilterDirection direction)
    {
        return ITransportFilter.allowsMoving(filter, type, resource, direction);
    }

    // Going in (INSERT), the common case
    public static <R extends Resource> boolean allows(ItemStack filter, TransportType<R> type, R resource)
    {
        return allows(filter, type, resource, FilterDirection.INSERT);
    }

    // Items going in, the most common case
    public static boolean allows(ItemStack filter, ItemResource resource)
    {
        return allows(filter, TransportTypes.ITEMS.get(), resource, FilterDirection.INSERT);
    }

    // Whether the item holding a filter lets this resource move this way
    public static <R extends Resource> boolean holderAllows(ItemStack holder, TransportType<R> type, R resource, FilterDirection direction)
    {
        return allows(get(holder), type, resource, direction);
    }

    // Whether the item holding a filter lets this item in
    public static boolean holderAllows(ItemStack holder, ItemResource resource)
    {
        return allows(get(holder), resource);
    }

    // Whether the item holding a filter lets this item be taken out
    public static boolean holderAllowsExtract(ItemStack holder, ItemResource resource)
    {
        return holderAllows(holder, TransportTypes.ITEMS.get(), resource, FilterDirection.EXTRACT);
    }

    // The most of a resource the item may store with its filter (a restricted filter...): only INSERT filters limit it
    public static <R extends Resource> int holderLimit(ItemStack holder, TransportType<R> type, R resource, int capacity)
    {
        return ITransportFilter.limitStored(get(holder), type, resource, capacity);
    }

    // The filter in/out clicks (see the class comment). mayInsert: whether this item takes a filter right now (a
    // take-out-only item says no). Returns the result to give back from use(), or null if this wasn't a filter click.
    public static @Nullable InteractionResult useOnHeld(Level level, Player player, InteractionHand hand, boolean mayInsert)
    {
        if (hand != InteractionHand.MAIN_HAND) return null;
        ItemStack holder = player.getMainHandItem();
        ItemStack offhand = player.getOffhandItem();
        if (isFilter(offhand))
        {
            if (!mayInsert || !get(holder).isEmpty()) return InteractionResult.FAIL;
            if (!level.isClientSide())
            {
                set(holder, offhand.split(1));
                level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 1.2f);
            }
            return InteractionResult.SUCCESS;
        }
        if (offhand.getItem() instanceof FilterToolItem)
        {
            if (!level.isClientSide())
            {
                ItemStack filter = get(holder);
                if (filter.isEmpty())
                {
                    // The same message a block without a filter gives (StorageInteractions.removeFilter)
                    player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".tool_filter_missing"));
                }
                else
                {
                    set(holder, ItemStack.EMPTY);
                    StorageInteractions.giveToPlayer(player, filter);
                    level.playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 0.8f);
                }
            }
            return InteractionResult.SUCCESS;
        }
        return null;
    }

    // For showing a whitelist-filtered paged item: each item it holds with its total over all pages, then the
    // filter's other items at 0. Null (show it page by page instead) without an INSERT filter (only those decide what
    // it holds), with a blacklist, or when it holds something the filter doesn't allow.
    public static @Nullable List<ResourceStack<ItemResource>> summary(ItemStack holder, PagedContents contents)
    {
        ItemStack filter = get(holder);
        if (!ITransportFilter.applies(filter, FilterDirection.INSERT)) return null;
        if (((ITransportFilter) filter.getItem()).isBlacklist(filter)) return null;
        // LinkedHashMap: keeps the order things were first found in; merge adds to an item already there
        Map<ItemResource, Integer> totals = new LinkedHashMap<>();
        for (ResourceStack<ItemResource> stack : contents.stacks())
        {
            if (stack.isEmpty()) continue;
            if (!allows(filter, stack.resource())) return null;
            totals.merge(stack.resource(), stack.amount(), Integer::sum);
        }
        for (ItemStackTemplate template : BaseFilterItem.getEntry(filter, TransportTypes.ITEMS.get()).list())
        {
            ItemStack listed = template.create();
            if (!listed.isEmpty() && !isFilter(listed)) totals.putIfAbsent(ItemResource.of(listed), 0);
        }
        List<ResourceStack<ItemResource>> summary = new ArrayList<>();
        totals.forEach((resource, amount) -> summary.add(new ResourceStack<>(resource, amount)));
        return summary;
    }
}
