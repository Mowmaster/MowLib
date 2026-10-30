package com.mowmaster.mowlib.items.tools;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

// A tool that stays in the crafting grid when used in a recipe, losing 1 durability each time, and breaks when it runs
// out (knives, hammers, chisels...). Give it durability through its properties, e.g.
//     props -> new CraftingToolItem(props.tool(ToolMaterial.STONE, MY_MINEABLE_TAG, 0.5f, -2.0f, 0f))
// (Item.Properties#tool sets durability, mining and attack data). Without durability it simply always comes back.
// (Moved from Bibliomania's KnifeItem.)
public class CraftingToolItem extends Item
{
    public CraftingToolItem(Properties properties)
    {
        super(properties);
    }

    // What's left in the grid after crafting. NeoForge's version of this takes the actual stack (an ItemInstance),
    // so we can copy its damage. Since 26.1 the result is an ItemStackTemplate (an immutable item + count + data).
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance)
    {
        if (!(instance instanceof ItemStack stack)) return new ItemStackTemplate(this, 1);
        if (!stack.isDamageableItem()) return ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1));

        ItemStack used = stack.copyWithCount(1);
        used.setDamageValue(used.getDamageValue() + 1);
        if (used.getDamageValue() >= used.getMaxDamage()) return null; // worn out: nothing comes back
        return ItemStackTemplate.fromNonEmptyStack(used);
    }
}
