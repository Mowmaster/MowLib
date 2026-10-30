package com.mowmaster.mowlib.paging;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.storage.CBBulkItemHandler;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;

import java.util.List;
import java.util.function.Predicate;

// What a paged storage item carries (the "mowlib:paged_contents" data component): its page count, the selected page,
// and what each page holds (one item type per page, any amount up to the page capacity, like CBBulkItemHandler).
// PagedStorage reads it when the item is placed and writes it when the block becomes an item again; IPagedStorageItem
// reads and writes it in inventories. Recipes can set it on their result, e.g. {"mowlib:paged_contents": {"pages": 12}}.
// Data components must be immutable and compare by value, which records do.
// (Moved from Bibliomania's BookContents; the saved format is unchanged.)
public record PagedContents(int pages, int page, List<ResourceStack<ItemResource>> stacks)
{
    public static final int DEFAULT_PAGES = 8;

    // Only "pages" is needed; a missing page or stack list means a new, empty item
    public static final Codec<PagedContents> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.intRange(1, 256).optionalFieldOf("pages", DEFAULT_PAGES).forGetter(PagedContents::pages),
                    Codec.INT.optionalFieldOf("page", 0).forGetter(PagedContents::page),
                    CBBulkItemHandler.STACK_CODEC.listOf().optionalFieldOf("stacks", List.of()).forGetter(PagedContents::stacks))
            .apply(instance, PagedContents::new));

    // How it's sent to clients (tooltips and item models need it): through the codec, like TransportContents.
    // "WithRegistries" because item resources refer to registered items.
    public static final StreamCodec<RegistryFriendlyByteBuf, PagedContents> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    // A compact constructor runs before the fields are set: copy the list so nobody can change it afterwards
    public PagedContents
    {
        stacks = List.copyOf(stacks);
    }

    public static PagedContents empty(int pages)
    {
        return new PagedContents(pages, 0, List.of());
    }

    // The selected page, always inside 0..pages-1
    public int selectedPage()
    {
        return Math.floorMod(page, pages);
    }

    public PagedContents withPage(int newPage)
    {
        return new PagedContents(pages, Math.floorMod(newPage, pages), stacks);
    }

    // What a page holds, or an empty stack for an empty (or missing) page
    public ResourceStack<ItemResource> stack(int index)
    {
        if (index >= 0 && index < stacks.size()) return stacks.get(index);
        return new ResourceStack<>(ItemResource.EMPTY, 0);
    }

    public long totalAmount()
    {
        long total = 0;
        for (ResourceStack<ItemResource> stack : stacks) total += stack.amount();
        return total;
    }

    // How full, 0 to 1, given how much each page holds
    public float fullness(int pageCapacity)
    {
        return (float) totalAmount() / ((long) pageCapacity * pages);
    }

    // A handler holding a copy of these pages, to reuse CBBulkItemHandler's rules (capacity, one item per page).
    // onChanged runs after each change made through the handler.
    public CBBulkItemHandler toHandler(int pageCapacity, Predicate<ItemResource> accepts, Runnable onChanged)
    {
        CBBulkItemHandler items = new CBBulkItemHandler(pages, pageCapacity, accepts, onChanged);
        for (int index = 0; index < Math.min(stacks.size(), pages); index++)
        {
            ResourceStack<ItemResource> stack = stacks.get(index);
            // set() overwrites a slot directly, without the capacity and filter checks of insert()
            if (!stack.isEmpty()) items.set(index, stack.resource(), stack.amount());
        }
        return items;
    }
}
