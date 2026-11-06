package com.mowmaster.mowlib.compacting;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

// Putting into and taking out of CompactingContents, for anything that holds them (a block entity, an item), and its
// tooltip lines. The contents are immutable: each call returns the changed contents with how much moved, for the
// holder to keep. (Moved from Bibliomania's Compacting Book.)
public final class Compacting
{
    private Compacting() {}

    // What a put-in or take-out did: the contents to keep, and how many moved
    public record Inserted(CompactingContents contents, int count) {}
    public record Extracted(CompactingContents contents, ItemStack stack) {}

    // Up to `count` of `resource` in. Empty contents first take the item's chain (CompactingChains.find), if `allowed`
    // says yes (a filter...), holding `capacity` of the top tier, and open at the tier that went in.
    public static Inserted insert(CompactingContents contents, ItemResource resource, int count, int capacity,
                                  Predicate<List<CompactingContents.Tier>> allowed)
    {
        if (resource.isEmpty() || count <= 0 || !resource.getComponentsPatch().isEmpty()) return new Inserted(contents, 0);
        CompactingContents working = contents;
        if (!working.isSet())
        {
            List<CompactingContents.Tier> chain = CompactingChains.find(resource.getItem());
            if (chain == null || !allowed.test(chain)) return new Inserted(contents, 0);
            working = CompactingContents.of(chain, contents.page(), capacity);
        }
        int tier = working.tierOf(resource);
        if (tier < 0) return new Inserted(contents, 0);
        int inserted = Math.min(count, working.room(tier));
        if (inserted <= 0) return new Inserted(contents, 0);
        if (!contents.isSet()) working = working.withPage(tier);
        return new Inserted(working.plus(tier, inserted), inserted);
    }

    // Up to `max` of a tier out (never more than a stack of it)
    public static Extracted extract(CompactingContents contents, int tier, int max)
    {
        int taken = Math.min(Math.min(max, contents.amount(tier)), contents.stack(tier, 1).getMaxStackSize());
        if (taken <= 0) return new Extracted(contents, ItemStack.EMPTY);
        return new Extracted(contents.plus(tier, -taken), contents.stack(tier, taken));
    }

    // "12 Block of Gold", "111 Gold Ingot", "1,000 Gold Nugget" (top tier first; the open one white), then "Holds up to
    // 1024 Block of Gold"; or, empty, how to start one
    public static void tooltip(CompactingContents contents, int capacity, Consumer<Component> tooltip)
    {
        String key = "tooltip." + MowLibReferences.MODID + ".compacting.";
        if (!contents.isSet())
        {
            tooltip.accept(Component.translatable(key + "empty", capacity).withStyle(ChatFormatting.GRAY));
            return;
        }
        for (int tier = contents.size() - 1; tier >= 0; tier--)
        {
            Component line = Component.translatable(key + "tier", contents.amount(tier), contents.stack(tier, 1).getHoverName());
            tooltip.accept(tier == contents.page() ? line.copy().withStyle(ChatFormatting.WHITE) : line.copy().withStyle(ChatFormatting.GRAY));
        }
        tooltip.accept(Component.translatable(key + "capacity", contents.capacity(), contents.stack(contents.size() - 1, 1).getHoverName())
                .withStyle(ChatFormatting.DARK_GRAY));
    }
}
