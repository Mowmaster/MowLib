package com.mowmaster.mowlib.reading;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.filter.HeldFilters;
import com.mowmaster.mowlib.paging.IPagedStorageItem;
import com.mowmaster.mowlib.paging.PagedContents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Ready-made reading papers and lines, for IReadableBlock / IReadableItem implementations.
public final class ReadingPapers
{
    private ReadingPapers() {}

    // "hud.mowlib.<key>": MowLib's own HUD lines
    public static MutableComponent hud(String key, Object... args)
    {
        return Component.translatable("hud." + MowLibReferences.MODID + "." + key, args);
    }

    // An item's paper, the way it is wherever it's shown: its own (IReadableItem), a paged storage item's
    // (pagedItem), or else its plain tooltip with any enchantments/potions spelled out
    public static ReadingPaper item(ItemStack stack, Level level, Player player)
    {
        if (stack.getItem() instanceof IReadableItem readable)
        {
            ReadingPaper paper = readable.read(stack, level, player);
            if (paper != null) return paper;
        }
        if (stack.getItem() instanceof IPagedStorageItem) return pagedItem(stack, level, player, List.of(), List.of());
        return ReadingPaper.of(tooltip(stack, level, player));
    }

    // A paged storage item (IPagedStorageItem):
    // - with a whitelist filter (HeldFilters) and only allowed items in it: its name, the filter, afterName, then each
    //   item it holds with its total over all pages (no page grid)
    // - otherwise: "Name (Page 3/16)", what's on the open page, the filter (if any), afterName, the rest of its
    //   tooltip, then the open page's item's own tooltip if it has more to say (enchantments, potions), and the page
    //   grid under it all
    // afterName: extra lines under the name (a priority...); shown: other items whose tooltips are worth adding (a
    // trade's items...)
    public static ReadingPaper pagedItem(ItemStack stack, Level level, Player player, List<Component> afterName, List<ItemStack> shown)
    {
        if (!(stack.getItem() instanceof IPagedStorageItem paged)) return ReadingPaper.of(tooltip(stack, level, player));
        PagedContents pages = paged.contents(stack);
        ItemStack filter = HeldFilters.get(stack);
        List<ResourceStack<ItemResource>> summary = HeldFilters.summary(stack, pages);
        List<Component> lines = new ArrayList<>();
        if (summary != null)
        {
            lines.add(stack.getHoverName());
            lines.add(hud("filter", filter.getHoverName()));
            lines.addAll(afterName);
            for (ResourceStack<ItemResource> each : summary) lines.add(hud("page_item", each.amount(), each.resource().toStack(1).getHoverName()));
            return ReadingPaper.of(lines);
        }
        List<Component> tooltip = tooltip(stack, level, player);
        int page = pages.selectedPage();
        lines.add(hud("page", tooltip.isEmpty() ? stack.getHoverName() : tooltip.getFirst(), page + 1, pages.pages()));
        ResourceStack<ItemResource> open = pages.stack(page);
        lines.add(open.isEmpty() ? hud("empty_page") : hud("page_item", open.amount(), open.resource().getHoverName()));
        if (!filter.isEmpty()) lines.add(hud("filter", filter.getHoverName()));
        lines.addAll(afterName);
        // getFirst / subList: the tooltip without its first line (the name, already used above)
        if (tooltip.size() > 1) lines.addAll(tooltip.subList(1, tooltip.size()));
        List<ItemStack> extra = new ArrayList<>(shown);
        if (!open.isEmpty()) extra.add(open.resource().toStack(1));
        addWorthReading(lines, extra, level, player);
        return ReadingPaper.withPages(lines, pages);
    }

    // The same lines an inventory tooltip has (name first), not the advanced (F3+H) ones
    public static List<Component> tooltip(ItemStack stack, Level level, Player player)
    {
        return stack.getTooltipLines(Item.TooltipContext.of(level), player, TooltipFlag.NORMAL);
    }

    // Each item that has more to say (worthReading), after a blank line, as its own tooltip
    public static void addWorthReading(List<Component> lines, List<ItemStack> items, Level level, Player player)
    {
        for (ItemStack item : items)
        {
            if (!worthReading(item)) continue;
            lines.add(Component.empty());
            lines.addAll(tooltip(item, level, player));
        }
    }

    // Enchanted items, enchanted books (stored enchantments), potions and tipped arrows
    public static boolean worthReading(@Nullable ItemStack stack)
    {
        return stack != null && !stack.isEmpty() && (stack.isEnchanted() || stack.has(DataComponents.STORED_ENCHANTMENTS)
                || stack.has(DataComponents.POTION_CONTENTS));
    }
}
