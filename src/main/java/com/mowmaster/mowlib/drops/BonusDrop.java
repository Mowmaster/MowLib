package com.mowmaster.mowlib.drops;

import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.function.Predicate;
import java.util.function.Supplier;

// An extra drop when a player breaks a block: if the tool and block match, `chance` (0-1) of dropping `drop`, on top
// of the block's normal drops. e.g. a knife on grass -> 20% grass fibers. Register them with BonusDrops.register.
// - tool: what the main hand must hold (Predicate<ItemStack> is a function from a stack to true/false)
// - block: which blocks it applies to
// - drop: made fresh each time (a Supplier is a function with no arguments that returns a value; it lets the drop
//   name a mod item that isn't registered yet when this is created). Any item, any count, any data.
// Creative players never get bonus drops (they get no drops at all).
// (Generalised from Bibliomania's GrassFiberHandler.)
public record BonusDrop(Predicate<ItemStack> tool, Predicate<BlockState> block, float chance, Supplier<ItemStack> drop)
{
    // The common case: a tool from an item tag, a block from a block tag
    public static BonusDrop of(TagKey<Item> tools, TagKey<Block> blocks, float chance, Supplier<ItemStack> drop)
    {
        return new BonusDrop(stack -> stack.is(tools), state -> state.is(blocks), chance, drop);
    }

    // Any tool (or an empty hand)
    public static BonusDrop anyTool(TagKey<Block> blocks, float chance, Supplier<ItemStack> drop)
    {
        return new BonusDrop(stack -> true, state -> state.is(blocks), chance, drop);
    }

    public boolean matches(Player player, BlockState state)
    {
        return tool.test(player.getMainHandItem()) && block.test(state);
    }
}
