package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.MowLibIds;
import com.mowmaster.mowlib.filter.FilterData;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.filter.ITransportFilter;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemUtil;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

// Shared filter behaviour (1.20's MowLibBaseFilter + BaseFilter). With the filter in the OFFHAND:
//   right-click the air            = toggle whitelist/blacklist for the current mode
//   sneak + right-click the air    = next mode (one per transport type: items, fluids, energy, experience, ...)
//   sneak + right-click a container = set the current mode's list from its contents
//   right-click a filter holder    = put the filter in, e.g. a pedestal (the filter tool in the offhand takes it out)
// Subclasses decide what "matches" means; a match is let through by a whitelist and blocked by a blacklist.
public abstract class BaseFilterItem extends Item implements ITransportFilter
{
    private final FilterDirection direction;

    protected BaseFilterItem(Properties properties, FilterDirection direction)
    {
        // New filters start in item mode; the default component also gives the item model its first texture
        super(properties.stacksTo(1)
                .component(MowLibDataComponents.FILTER_MODE.get(), MowLibIds.id("items")));
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

    // Whether sneak-right-clicking a container sets the list for this type
    protected boolean usesListFor(TransportType<?> type)
    {
        return type.usesFilterList();
    }

    protected boolean canToggleBlacklist()
    {
        return true;
    }

    // What whitelist/blacklist are called for this filter (the durability filter says above/below)
    protected Component listTypeName(boolean blacklist)
    {
        return Component.translatable(MowLibIds.MODID + (blacklist ? ".filter_type_blacklist" : ".filter_type_whitelist"));
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
            if (!level.isClientSide()) player.sendOverlayMessage(Component.translatable(MowLibIds.MODID + ".filter.message_twohanded").withStyle(ChatFormatting.RED));
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

    // Sneak + right-click a container: its contents become the list for the current mode
    @Override
    public InteractionResult useOn(UseOnContext context)
    {
        Player player = context.getPlayer();
        if (player == null || context.getHand() != InteractionHand.OFF_HAND || !player.isShiftKeyDown()) return InteractionResult.PASS;

        Level level = context.getLevel();
        ItemStack filter = context.getItemInHand();
        TransportType<?> mode = getMode(filter);
        if (!usesListFor(mode)) return InteractionResult.PASS;

        ResourceHandler<ItemResource> inventory = level.getCapability(Capabilities.Item.BLOCK, context.getClickedPos(), context.getClickedFace());
        if (inventory == null) return InteractionResult.PASS;

        if (!level.isClientSide())
        {
            List<ItemStackTemplate> list = new ArrayList<>();
            for (int index = 0; index < inventory.size(); index++)
            {
                ItemStack stack = ItemUtil.getStack(inventory, index);
                if (!stack.isEmpty()) list.add(ItemStackTemplate.fromNonEmptyStack(stack));
            }
            FilterData data = getData(filter);
            filter.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), data.get(mode.getId()).withList(list)));
            player.sendOverlayMessage(Component.translatable(MowLibIds.MODID + ".filter_changed").withStyle(mode.getColor()));
        }
        return InteractionResult.SUCCESS;
    }

    private static void cycleMode(Player player, ItemStack filter)
    {
        List<TransportType<?>> types = TransportTypes.REGISTRY.stream().toList();
        int next = (types.indexOf(getMode(filter)) + 1) % types.size();
        TransportType<?> mode = types.get(next);
        filter.set(MowLibDataComponents.FILTER_MODE.get(), mode.getId());
        player.sendOverlayMessage(Component.translatable(MowLibIds.MODID + ".filter_mode_changed").append(mode.getName()));
    }

    private void toggleBlacklist(Player player, ItemStack filter)
    {
        TransportType<?> mode = getMode(filter);
        FilterData data = getData(filter);
        FilterData.Entry entry = data.get(mode.getId());
        boolean blacklist = !entry.blacklist();
        filter.set(MowLibDataComponents.FILTER_DATA.get(), data.with(mode.getId(), entry.withBlacklist(blacklist)));
        player.sendOverlayMessage(Component.translatable(MowLibIds.MODID + ".filter_type_changed")
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

        tooltip.accept(Component.translatable(MowLibIds.MODID + ".filter.tooltip_filterdirection").append(direction.getName()).withStyle(ChatFormatting.GRAY));
        tooltip.accept(Component.translatable(MowLibIds.MODID + ".tooltip_mode").append(mode.getName()));
        if (canToggleBlacklist())
        {
            tooltip.accept(Component.translatable(MowLibIds.MODID + ".filter_type").append(listTypeName(entry.blacklist())).withStyle(ChatFormatting.WHITE));
        }
        for (ItemStackTemplate template : entry.list())
        {
            ItemStack listed = template.create();
            tooltip.accept(Component.literal(" - ").append(listed.getHoverName()).append(" x" + listed.getCount()).withStyle(ChatFormatting.DARK_GRAY));
        }
    }
}
