package com.mowmaster.mowlib.knapping;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;
import java.util.function.Supplier;

// One knapping recipe: hold something in each hand, right-click a block, get an item (or block item).
// e.g. flint + flint on stone -> a knife blade. Register them with KnappingRecipes.register.
// - mainhand / offhand: what must be held (Predicate<ItemStack> is a function from a stack to true/false)
// - target: which blocks it works on
// - result: made fresh each time (a Supplier is a function with no arguments that returns a value; it lets the result
//   name a mod item that isn't registered yet when the recipe is created)
// - consumeMainhand / consumeOffhand: whether one item is used up from that hand (creative players keep theirs)
// (Generalised from Bibliomania's KnappingHandler, which replaced 1.20 MowLib's dual-handed crafting.)
public record KnappingRecipe(Predicate<ItemStack> mainhand, Predicate<ItemStack> offhand, Predicate<BlockState> target,
                             Supplier<ItemStack> result, boolean consumeMainhand, boolean consumeOffhand)
{
    // The common case: one item in each hand (both used up), any block in a tag
    public static KnappingRecipe of(ItemLike mainhand, ItemLike offhand, TagKey<Block> target, Supplier<ItemStack> result)
    {
        return new KnappingRecipe(holding(mainhand), holding(offhand), state -> state.is(target), result, true, true);
    }

    public static Predicate<ItemStack> holding(ItemLike item)
    {
        return stack -> stack.is(item.asItem());
    }

    public static Predicate<ItemStack> holding(TagKey<Item> tag)
    {
        return stack -> stack.is(tag);
    }

    public boolean matches(Player player, BlockState state)
    {
        return mainhand.test(player.getMainHandItem()) && offhand.test(player.getOffhandItem()) && target.test(state);
    }
}
