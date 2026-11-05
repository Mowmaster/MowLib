package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.FilterData;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.filter.ITransportFilter;
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
//   scroll over the filter, or the page keys over it or while holding it = select the next/previous list item
//                                                                     (IPageable; the tooltip marks it, and turning
//                                                                     it in the hand names it over the hotbar)
// Subclasses decide what "matches" means; a match is let through by a whitelist and blocked by a blacklist.
public abstract class BaseFilterItem extends Item implements ITransportFilter, IInventoryUsable, IPageable
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
            if (player.isShiftKeyDown()) cycleMode(player, filter);
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
        TransportType<?> mode = getMode(self);
        if (!usesListFor(mode)) return false;
        FilterData data = getData(self);
        FilterData.Entry entry = data.get(mode.getId());
        List<ItemStackTemplate> list = new ArrayList<>(entry.list());

        if (action == ClickAction.PRIMARY)
        {
            if (other.isEmpty()) return false;
            if (!acceptsListEntry(self, other))
            {
                InventoryClicks.playFail(player);
                return true;
            }
            // The same item (same data) already listed: replaced, so its count is updated rather than listed twice
            boolean replaced = list.removeIf(listed -> ItemStack.isSameItemSameComponents(listed.create(), other));
            if (!replaced && list.size() >= MAX_LIST_SIZE)
            {
                InventoryClicks.playFail(player);
                return true;
            }
            // fromNonEmptyStack: an immutable copy (item, count, data); the clicked stack itself is left alone
            list.add(ItemStackTemplate.fromNonEmptyStack(other));
            // The item just added becomes the selected one
            self.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), entry.withList(list, list.size() - 1)));
            InventoryClicks.playInsert(player);
            return true;
        }

        // Right-click: with an item, take that item off; with nothing, the selected one
        boolean removed;
        if (other.isEmpty())
        {
            removed = !list.isEmpty();
            if (removed) list.remove(entry.selectedIndex());
        }
        else removed = list.removeIf(listed -> ItemStack.isSameItemSameComponents(listed.create(), other));
        if (!removed)
        {
            InventoryClicks.playFail(player);
            return true;
        }
        setList(self, mode, data, entry, list);
        InventoryClicks.playRemove(player);
        return true;
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

    private static void cycleMode(Player player, ItemStack filter)
    {
        List<TransportType<?>> types = TransportTypes.REGISTRY.stream().toList();
        int next = (types.indexOf(getMode(filter)) + 1) % types.size();
        TransportType<?> mode = types.get(next);
        filter.set(MowLibDataComponents.FILTER_MODE.get(), mode.getId());
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
                .append(listTypeName(blacklist))
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
            tooltip.accept(Component.translatable(MowLibReferences.MODID + ".filter_type").append(listTypeName(entry.blacklist())).withStyle(ChatFormatting.WHITE));
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
