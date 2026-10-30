package com.mowmaster.mowlib.items.filters;

import com.mowmaster.mowlib.filter.FilterData;
import com.mowmaster.mowlib.filter.FilterDirection;
import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.resource.Resource;

// Filters that only make sense for items (durability, enchantments, food). Other modes follow the plain
// whitelist/blacklist rule from BaseFilterItem.
public abstract class ItemOnlyFilterItem extends BaseFilterItem
{
    protected ItemOnlyFilterItem(Properties properties)
    {
        super(properties, FilterDirection.INSERT);
    }

    protected abstract boolean matchesItem(ItemStack incoming, FilterData.Entry entry);

    // false for filters with no list (enchanted, food)
    protected boolean usesList()
    {
        return true;
    }

    @Override
    protected boolean handlesType(TransportType<?> type)
    {
        return type == TransportTypes.ITEMS.get();
    }

    @Override
    protected boolean usesListFor(TransportType<?> type)
    {
        return usesList() && handlesType(type);
    }

    @Override
    protected <R extends Resource> boolean matches(ItemStack filter, TransportType<R> type, R resource, FilterData.Entry entry)
    {
        // "instanceof ItemResource item" checks the type and names the cast value in one go (pattern matching)
        return resource instanceof ItemResource item && matchesItem(item.toStack(), entry);
    }
}
