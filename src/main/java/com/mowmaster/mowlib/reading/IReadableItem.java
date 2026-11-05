package com.mowmaster.mowlib.reading;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

// An item with its own reading paper: shown while it's held (and not looking at a readable block), and wherever a
// block shows it through ReadingPapers.item (a placed book, a book on a shelf). Without this, paged storage items
// (IPagedStorageItem) get MowLib's paged paper and other items their plain tooltip.
// Called on the client.
public interface IReadableItem
{
    @Nullable ReadingPaper read(ItemStack stack, Level level, Player player);

    // Whether holding it shows its paper (one on its own; the player's "held items" setting must be on too)
    default boolean readWhenHeld(ItemStack stack)
    {
        return true;
    }
}
