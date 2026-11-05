package com.mowmaster.mowlib.palette;

import com.mowmaster.mowlib.paging.IPageable;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
import com.mowmaster.mowlib.storage.IInventoryUsable;
import com.mowmaster.mowlib.storage.InventoryClick;
import com.mowmaster.mowlib.storage.InventoryClicks;
import com.mowmaster.mowlib.tint.IDyeSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemInstance;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemStackTemplate;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

// The Color Palette (mowlib:color_palette): a bundle-like item holding one of each dye (vanilla dyes and Custom Dyes),
// sorted by colour. Its selected dye is what it paints with: right-click any IColorableBlock with it (IDyeSource makes
// IColorableBlock#dyeColorOf return the selected colour), or use it in place of a dye in ColorableItemDyeRecipe (it
// comes back out of the grid). The item model is tinted with the selected colour (client/SelectedDyeTint).
//
// In an inventory, like a bundle:
// - Holding the palette, left-click a dye in a slot / holding a dye, left-click the palette: puts ONE of it in.
//   A dye the palette already has is used up and not stored (it only ever holds one of each).
// - Holding the palette, right-click an empty slot / empty-handed, right-click the palette: takes the selected dye out.
// - Scroll over it, or press the page keys over it: selects the next/previous dye (IPageable, see PageInputHandler).
// Held in the hand, the page keys select the next/previous dye too. Unlike a bundle, right-clicking with it in the
// world never empties it.
public class ColorPaletteItem extends Item implements IDyeSource, IPageable, IInventoryUsable
{
    public ColorPaletteItem(Properties properties)
    {
        super(properties.stacksTo(1));
    }

    public static ColorPaletteContents contents(ItemStack palette)
    {
        return palette.getOrDefault(MowLibDataComponents.COLOR_PALETTE.get(), ColorPaletteContents.EMPTY);
    }

    public static void setContents(ItemStack palette, ColorPaletteContents contents)
    {
        palette.set(MowLibDataComponents.COLOR_PALETTE.get(), contents);
    }

    // ---- IDyeSource: what it paints with ----

    @Override
    public @Nullable Integer getDyeColor(ItemStack stack)
    {
        return contents(stack).selectedColor();
    }

    // ---- IPageable: the "pages" are the dyes, the selected page is the selected dye ----

    @Override
    public int getPageCount(ItemStack stack)
    {
        return Math.max(1, contents(stack).size());
    }

    @Override
    public int getPage(ItemStack stack)
    {
        return contents(stack).selectedIndex();
    }

    @Override
    public void setPage(ItemStack stack, int page)
    {
        setContents(stack, contents(stack).withSelected(page));
    }

    // ---- Putting dyes in and taking them out ----

    // What happens when a dye is offered to the palette
    public enum InsertResult
    {
        STORED,     // a new dye: one went in
        DUPLICATE,  // already held: one is used up and voided
        REJECTED    // not a dye, or the palette is full: nothing happens
    }

    // What would happen if one of `dye` were offered (changes nothing)
    public static InsertResult check(ItemStack palette, ItemStack dye)
    {
        if (!ColorPaletteContents.isDye(dye)) return InsertResult.REJECTED;
        ColorPaletteContents contents = contents(palette);
        if (contents.contains(dye)) return InsertResult.DUPLICATE;
        return contents.isFull() ? InsertResult.REJECTED : InsertResult.STORED;
    }

    // Offers one of `dye` to the palette. Doesn't change `dye`: on STORED or DUPLICATE the caller removes one from it.
    public static InsertResult insert(ItemStack palette, ItemStack dye)
    {
        InsertResult result = check(palette, dye);
        if (result == InsertResult.STORED) setContents(palette, contents(palette).withAdded(dye));
        return result;
    }

    // Takes the selected dye out (EMPTY if the palette is empty)
    public static ItemStack extractSelected(ItemStack palette)
    {
        ColorPaletteContents contents = contents(palette);
        if (contents.isEmpty()) return ItemStack.EMPTY;
        ItemStack taken = contents.selectedStack();
        setContents(palette, contents.withoutSelected());
        return taken;
    }

    // ---- Bundle-style inventory clicks, through MowLib's shared InventoryClicks (the guards and syncing) ----
    // These run on both sides; the client predicts, like vanilla's bundle.

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

    // Right-click an empty slot with the palette: the selected dye comes out into it
    @Override
    public boolean usedOnEmptySlots(ItemStack self)
    {
        return true;
    }

    // `other` is the item clicked with or onto (or nothing); `click` says where it is
    @Override
    public boolean usedWith(ItemStack self, ItemStack other, ClickAction action, Player player, InventoryClick click)
    {
        // Left-click with/onto a dye: one goes in. Anything that isn't a dye: the click does what it normally would.
        if (action == ClickAction.PRIMARY && ColorPaletteContents.isDye(other))
        {
            InsertResult result = check(self, other);
            if (result != InsertResult.REJECTED)
            {
                // take: from a slot through safeTake (EMPTY from slots you can't take from)
                ItemStack taken = click.take(other, 1, player);
                if (taken.isEmpty()) result = InsertResult.REJECTED;
                else insert(self, taken);
            }
            playInsertSound(player, result);
            return true;
        }
        // Right-click onto an empty slot, or with an empty cursor: the selected dye comes out there
        if (action == ClickAction.SECONDARY && other.isEmpty())
        {
            ItemStack removed = extractSelected(self);
            if (!removed.isEmpty())
            {
                ItemStack left = click.putTaken(removed, player);
                if (!left.isEmpty()) insert(self, left); // it didn't fit there: put it back
                InventoryClicks.playRemove(player);
            }
            return true;
        }
        return false;
    }

    // Every dye, drawn by client/ClientColorPaletteTooltip
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack)
    {
        return Optional.of(new ColorPaletteTooltip(contents(stack)));
    }

    // Used in place of a dye in a crafting grid: the palette comes back, contents and all
    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemInstance instance)
    {
        if (instance instanceof ItemStack stack && !stack.isEmpty()) return ItemStackTemplate.fromNonEmptyStack(stack.copyWithCount(1));
        return new ItemStackTemplate(this, 1);
    }

    // ---- Sounds ----

    private static void playInsertSound(Player player, InsertResult result)
    {
        switch (result)
        {
            case STORED -> InventoryClicks.playInsert(player);
            // Lower pitch: it went in, but was thrown away
            case DUPLICATE -> player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.5f);
            case REJECTED -> InventoryClicks.playFail(player);
        }
    }
}
