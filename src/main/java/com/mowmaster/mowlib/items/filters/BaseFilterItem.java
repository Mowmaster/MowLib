package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.FilterData;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.keys.IActivatable;
import com.mowmaster.mowlib.keys.IBlockKeyTool;
import com.mowmaster.mowlib.paging.IPageable;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.storage.IInventoryUsable;
import com.mowmaster.mowlib.storage.InventoryClick;
import com.mowmaster.mowlib.storage.InventoryClicks;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.neoforge.transfer.resource.Resource;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Shared filter behaviour (1.20's MowLibBaseFilter + BaseFilter). With the filter in the OFFHAND:
//   right-click the air            = toggle whitelist/blacklist for the current mode
//   sneak + right-click the air    = next mode (one per transport type: items, fluids, energy, experience, ...)
//   right-click a filter holder    = put the filter in, e.g. a pedestal (the filter tool in the offhand takes it out)
// Setting the current mode's list, in an inventory, like a bundle (InventoryClicks), but nothing is ever used up:
// the clicked item only shows the filter what to list, and stays where it is.
//   left-click an item onto the filter, or the filter onto an item  = add it to the list (with its count, which the
//                                                                     restricted filter adds up; clicking the same
//                                                                     item again updates its count)
//   right-click an item onto the filter, or the filter onto an item = take that item off the list
//   right-click the filter with an empty cursor, or the filter onto an empty slot = take the SELECTED item off
//   scroll over the filter, or the page keys over it  = select the next/previous list item (IPageable; the tooltip
//                                                       marks it)
// MowLib's keys (client/KeyActionHandler, PageInputHandler):
//   page keys while holding it  = next/previous mode
//   activation key (held, or over it in an inventory) = whitelist <-> blacklist, or allowed <-> disabled
//   pick-up key at a block      = list that block (its item; in fluids mode a bucket of its fluid)
//   place key at a block        = take it off the list
// Subclasses decide what "matches" means; a match is let through by a whitelist and blocked by a blacklist.
public abstract class BaseFilterItem extends Item implements ITransportFilter, IInventoryUsable, IPageable, IActivatable, IBlockKeyTool
{
    // The most items one mode's list holds (a chest's worth)
    public static final int MAX_LIST_SIZE = 27;

    private final FilterDirection direction;

    protected BaseFilterItem(Properties properties, FilterDirection direction)
    {
        // New filters start in item mode; the default component also gives the item model its first texture
        super(properties.stacksTo(1)
                .component(MowLibDataComponents.FILTER_MODE.get(), MowLibReferences.id("items")));
        this.direction = direction;
    }

    // ---- What subclasses define ----

    // Does the resource match this filter's list (or rule) for this type?
    protected abstract <R extends Resource> boolean matches(ItemStack filter, TransportType<R> type, R resource, FilterData.Entry entry);

    // Whether this filter means anything for the type. If not, a whitelist lets everything of that type through
    // and a blacklist blocks all of it (1.20 behaviour: e.g. a blacklisted energy mode blocks all energy).
    protected boolean handlesType(TransportType<?> type)
    {
        return type.usesFilterList();
    }

    // Whether items can be clicked onto the filter to set the list for this type (when not, clicks do what they
    // normally would)
    protected boolean usesListFor(TransportType<?> type)
    {
        return type.usesFilterList();
    }

    // Whether this item may go on the list (e.g. the tag filters only take Tag Getters). Refused with the bundle's
    // fail sound.
    protected boolean acceptsListEntry(ItemStack filter, ItemStack entry)
    {
        return !entry.isEmpty();
    }

    protected boolean canToggleBlacklist()
    {
        return true;
    }

    // What whitelist/blacklist are called for this filter (the durability filter says above/below)
    protected Component listTypeName(boolean blacklist)
    {
        return Component.translatable(MowLibReferences.MODID + (blacklist ? ".filter_type_blacklist" : ".filter_type_whitelist"));
    }

    // The same for a mode: amount-only types (energy, experience) are simply Allowed or Disabled
    protected Component listTypeName(TransportType<?> mode, boolean blacklist)
    {
        if (mode.isAmountOnly()) return Component.translatable(MowLibReferences.MODID + (blacklist ? ".filter_type_disabled" : ".filter_type_allowed"));
        return listTypeName(blacklist);
    }

    // Whether this stack is a filter whose current mode is a blacklist (or disabled), for the item model: the black
    // frame tint (client/FilterListTint) and the plain icon for a disabled amount-only mode (client/FilterBlacklistProperty).
    // Filters that can't be toggled (restricted) never are.
    public static boolean isBlacklisted(ItemStack stack)
    {
        return stack.getItem() instanceof BaseFilterItem filter && filter.canToggleBlacklist() && getEntry(stack, getMode(stack)).blacklist();
    }

    // ---- ITransportFilter ----

    @Override
    public FilterDirection getDirection(ItemStack filter)
    {
        return direction;
    }

    @Override
    public <R extends Resource> boolean allows(ItemStack filter, TransportType<R> type, R resource)
    {
        FilterData.Entry entry = getEntry(filter, type);
        boolean whitelist = !entry.blacklist();
        if (!handlesType(type)) return whitelist;
        return matches(filter, type, resource, entry) == whitelist;
    }

    // A restricted filter placed in another filter's list limits the amount (1.20 behaviour)
    @Override
    public <R extends Resource> int limit(ItemStack filter, TransportType<R> type, R resource, int capacity)
    {
        for (ItemStackTemplate template : getEntry(filter, type).list())
        {
            ItemStack entryStack = template.create();
            if (entryStack.getItem() instanceof RestrictedFilterItem restricted)
            {
                return restricted.limit(entryStack, type, resource, capacity);
            }
        }
        return capacity;
    }

    @Override
    public boolean isBlacklist(ItemStack filter)
    {
        return getEntry(filter, getMode(filter)).blacklist();
    }

    // ---- Reading the settings ----

    public static TransportType<?> getMode(ItemStack filter)
    {
        Identifier id = filter.get(MowLibDataComponents.FILTER_MODE.get());
        TransportType<?> type = id == null ? null : TransportTypes.REGISTRY.getValue(id);
        return type != null ? type : TransportTypes.ITEMS.get();
    }

    public static FilterData getData(ItemStack filter)
    {
        return filter.getOrDefault(MowLibDataComponents.FILTER_DATA.get(), FilterData.EMPTY);
    }

    public static FilterData.Entry getEntry(ItemStack filter, TransportType<?> type)
    {
        return getData(filter).get(type.getId());
    }

    // The list as resources of a type (items that mean nothing for that type are left out)
    protected static <R extends Resource> List<R> listResources(TransportType<R> type, FilterData.Entry entry)
    {
        List<R> resources = new ArrayList<>();
        for (ItemStackTemplate template : entry.list())
        {
            R resource = type.resourceFromFilterEntry(template.create());
            if (resource != null && !resource.isEmpty()) resources.add(resource);
        }
        return resources;
    }

    // ---- Using the filter ----

    @Override
    public InteractionResult use(Level level, Player player, InteractionHand hand)
    {
        if (hand != InteractionHand.OFF_HAND) return InteractionResult.PASS;
        if (player.getMainHandItem().getItem() instanceof ITransportFilter)
        {
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".filter.message_twohanded").withStyle(ChatFormatting.RED));
            return InteractionResult.FAIL;
        }
        if (!level.isClientSide())
        {
            ItemStack filter = player.getOffhandItem();
            if (player.isShiftKeyDown()) announceMode(player, cycleMode(filter, 1));
            else if (canToggleBlacklist()) toggleBlacklist(player, filter);
        }
        return InteractionResult.SUCCESS;
    }

    // ---- Setting the list: bundle-style inventory clicks (InventoryClicks does the guards and syncing) ----
    // These run on both sides (the client shows the change straight away, like vanilla's bundle). Nothing is used up.

    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player)
    {
        return InventoryClicks.stackedOnOther(this, self, slot, clickAction, player);
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction,
                                            Player player, SlotAccess carriedItem)
    {
        return InventoryClicks.otherStackedOnMe(this, self, other, slot, clickAction, player, carriedItem);
    }

    // The filter onto an empty slot: a right-click takes the last item off the list (a left-click puts it down)
    @Override
    public boolean usedOnEmptySlots(ItemStack self)
    {
        return true;
    }

    // `other` is the item clicked with or onto (or nothing). It's only looked at, never changed.
    @Override
    public boolean usedWith(ItemStack self, ItemStack other, ClickAction action, Player player, InventoryClick click)
    {
        if (!usesListFor(getMode(self))) return false;
        if (action == ClickAction.PRIMARY)
        {
            if (other.isEmpty()) return false;
            if (addToList(self, other) == ListChange.DONE) InventoryClicks.playInsert(player);
            else InventoryClicks.playFail(player);
            return true;
        }
        // Right-click: with an item, take that item off; with nothing, the selected one
        if (removeFromList(self, other) == ListChange.DONE) InventoryClicks.playRemove(player);
        else InventoryClicks.playFail(player);
        return true;
    }

    // ---- Changing the current mode's list (the inventory clicks and the pick-up / place keys) ----

    // How a change to the list went
    public enum ListChange { DONE, REFUSED, FULL, MISSING }

    // Lists a copy of `entry` (with its count) in the current mode; the same item already listed is replaced, so its
    // count is updated rather than listed twice. The item just added becomes the selected one. `entry` isn't changed.
    public ListChange addToList(ItemStack filter, ItemStack entry)
    {
        if (entry.isEmpty() || !acceptsListEntry(filter, entry)) return ListChange.REFUSED;
        TransportType<?> mode = getMode(filter);
        FilterData data = getData(filter);
        FilterData.Entry modeEntry = data.get(mode.getId());
        List<ItemStackTemplate> list = new ArrayList<>(modeEntry.list());
        boolean replaced = list.removeIf(listed -> ItemStack.isSameItemSameComponents(listed.create(), entry));
        if (!replaced && list.size() >= MAX_LIST_SIZE) return ListChange.FULL;
        // fromNonEmptyStack: an immutable copy (item, count, data)
        list.add(ItemStackTemplate.fromNonEmptyStack(entry));
        filter.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), modeEntry.withList(list, list.size() - 1)));
        return ListChange.DONE;
    }

    // Takes `entry` (the same item, same data) off the current mode's list; EMPTY takes the selected one off
    public ListChange removeFromList(ItemStack filter, ItemStack entry)
    {
        TransportType<?> mode = getMode(filter);
        FilterData data = getData(filter);
        FilterData.Entry modeEntry = data.get(mode.getId());
        List<ItemStackTemplate> list = new ArrayList<>(modeEntry.list());
        boolean removed;
        if (entry.isEmpty())
        {
            removed = !list.isEmpty();
            if (removed) list.remove(modeEntry.selectedIndex());
        }
        else removed = list.removeIf(listed -> ItemStack.isSameItemSameComponents(listed.create(), entry));
        if (!removed) return ListChange.MISSING;
        setList(filter, mode, data, modeEntry, list);
        return ListChange.DONE;
    }

    private static void setList(ItemStack filter, TransportType<?> mode, FilterData data, FilterData.Entry entry, List<ItemStackTemplate> list)
    {
        filter.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), entry.withList(list)));
    }

    // ---- IPageable: the "pages" are the current mode's list items; the selected page is the selected item ----

    @Override
    public int getPageCount(ItemStack stack)
    {
        return Math.max(1, getEntry(stack, getMode(stack)).list().size());
    }

    @Override
    public int getPage(ItemStack stack)
    {
        return getEntry(stack, getMode(stack)).selectedIndex();
    }

    @Override
    public void setPage(ItemStack stack, int page)
    {
        TransportType<?> mode = getMode(stack);
        FilterData data = getData(stack);
        FilterData.Entry entry = data.get(mode.getId());
        if (!entry.list().isEmpty()) stack.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), entry.withSelected(page)));
    }

    // Over the hotbar when it's turned in the hand: "Selected: Cobblestone x16" (null with an empty list: MowLib's
    // plain "Page 1/1")
    @Override
    public @Nullable Component describePage(ItemStack stack, int page)
    {
        List<ItemStackTemplate> list = getEntry(stack, getMode(stack)).list();
        if (page < 0 || page >= list.size()) return null;
        return Component.translatable(MowLibReferences.MODID + ".filter.selected", describeListEntry(list.get(page).create()));
    }

    // How a list item is shown (tooltip, hotbar message): "Cobblestone x16". The tag filters show the Tag Getter's
    // selected tag instead.
    protected Component describeListEntry(ItemStack entry)
    {
        return Component.empty().append(entry.getHoverName()).append(" x" + entry.getCount());
    }

    // ---- MowLib's keys ----

    // Page keys while holding the filter: switch its mode (scrolling / the page keys over it in an inventory still
    // select list items). Both sides change it; the server says which mode it's now in.
    @Override
    public void turnHeld(ItemStack stack, int direction, Player player)
    {
        TransportType<?> mode = cycleMode(stack, direction);
        if (!player.level().isClientSide()) announceMode(player, mode);
    }

    // The held keys change the mode and say so themselves: no "Selected: ..." message over the hotbar
    @Override
    public boolean showsHeldPageMessages(ItemStack stack)
    {
        return false;
    }

    // Activation key (held, or over it in an inventory): whitelist <-> blacklist, or allowed <-> disabled
    @Override
    public boolean canActivate(ItemStack stack)
    {
        return canToggleBlacklist();
    }

    @Override
    public void activate(ItemStack stack, ServerPlayer player)
    {
        toggleBlacklist(player, stack);
    }

    // Pick-up key at a block: list it (its item, or for fluids a bucket of its fluid). Place key: take it off.
    // Only when the current mode lists things, the block means something for it (TransportType#filterEntryAt), and
    // this filter can list it (the tag filters take only Tag Getters, so they never use the keys).
    @Override
    public boolean usesBlockKey(ItemStack stack, Level level, BlockHitResult hit, Player player, BlockKey key)
    {
        TransportType<?> mode = getMode(stack);
        if (!usesListFor(mode)) return false;
        ItemStack entry = mode.filterEntryAt(level, hit.getBlockPos(), hit.getDirection());
        if (entry.isEmpty()) return false;
        return acceptsListEntry(stack, entry);
    }

    @Override
    public void onBlockKey(ItemStack stack, ServerPlayer player, BlockHitResult hit, BlockKey key)
    {
        ItemStack entry = getMode(stack).filterEntryAt(player.level(), hit.getBlockPos(), hit.getDirection());
        Component name = describeListEntry(entry);
        String prefix = MowLibReferences.MODID + ".filter.";
        ListChange change = key == BlockKey.PICKUP ? addToList(stack, entry) : removeFromList(stack, entry);
        Component message = switch (change)
        {
            case DONE -> Component.translatable(prefix + (key == BlockKey.PICKUP ? "block_added" : "block_removed"), name);
            case MISSING -> Component.translatable(prefix + "block_missing", name);
            case FULL -> Component.translatable(prefix + "list_full");
            case REFUSED -> Component.translatable(prefix + "block_refused", name);
        };
        player.sendOverlayMessage(message);
        if (change == ListChange.DONE)
        {
            player.level().playSound(null, player.blockPosition(), key == BlockKey.PICKUP ? SoundEvents.BUNDLE_INSERT : SoundEvents.BUNDLE_REMOVE_ONE,
                    SoundSource.PLAYERS, 0.8f, 1f);
        }
    }

    // The next (+1) or previous (-1) mode, in registry order, wrapping round. Returns the new mode.
    private static TransportType<?> cycleMode(ItemStack filter, int direction)
    {
        List<TransportType<?>> types = TransportTypes.REGISTRY.stream().toList();
        int next = Math.floorMod(types.indexOf(getMode(filter)) + direction, types.size());
        TransportType<?> mode = types.get(next);
        filter.set(MowLibDataComponents.FILTER_MODE.get(), mode.getId());
        return mode;
    }

    private static void announceMode(Player player, TransportType<?> mode)
    {
        player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".filter_mode_changed").append(mode.getName()));
    }

    private void toggleBlacklist(Player player, ItemStack filter)
    {
        TransportType<?> mode = getMode(filter);
        FilterData data = getData(filter);
        FilterData.Entry entry = data.get(mode.getId());
        boolean blacklist = !entry.blacklist();
        filter.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), entry.withBlacklist(blacklist)));
        player.sendOverlayMessage(Component.translatable(MowLibReferences.MODID + ".filter_type_changed")
                .append(listTypeName(mode, blacklist))
                .withStyle(blacklist ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE));
    }

    // ---- Tooltip: direction, current mode, whitelist/blacklist, and the list ----

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, TooltipDisplay display, Consumer<Component> tooltip, TooltipFlag flag)
    {
        super.appendHoverText(stack, context, display, tooltip, flag);
        TransportType<?> mode = getMode(stack);
        FilterData.Entry entry = getEntry(stack, mode);

        tooltip.accept(Component.translatable(MowLibReferences.MODID + ".filter.tooltip_filterdirection").append(direction.getName()).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(MowLibReferences.MODID + ".tooltip_mode").append(mode.getName()));
        if (canToggleBlacklist())
        {
            tooltip.accept(Component.translatable(MowLibReferences.MODID + ".filter_type").append(listTypeName(mode, entry.blacklist())).withStyle(ChatFormatting.WHITE));
        }
        // The list, the selected item (the one a right-click removes) marked and brighter
        int selected = entry.selectedIndex();
        for (int index = 0; index < entry.list().size(); index++)
        {
            boolean isSelected = index == selected;
            tooltip.accept(Component.literal(isSelected ? " > " : " - ").append(describeListEntry(entry.list().get(index).create()))
                    .withStyle(isSelected ? ChatFormatting.GRAY : ChatFormatting.DARK_GRAY));
        }
        // How to set it, while this mode's list is empty
        if (entry.list().isEmpty() && usesListFor(mode))
        {
            tooltip.accept(Component.translatable(MowLibReferences.MODID + ".filter.tooltip_howto").withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
