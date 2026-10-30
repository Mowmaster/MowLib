package com.mowmaster.mowlib.palette;

import com.mowmaster.mowlib.paging.IPageable;
import com.mowmaster.mowlib.registry.MowLibDataComponents;
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
public class ColorPaletteItem extends Item implements IDyeSource, IPageable
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

    // ---- Bundle-style inventory clicks (these run on both sides; the client predicts, like vanilla's bundle) ----

    // Holding the palette on the cursor, clicking a slot
    @Override
    public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction clickAction, Player player)
    {
        if (self.getCount() != 1) return false;
        ItemStack other = slot.getItem();

        // Left-click a dye: one goes in. Anything that isn't a dye: the click does what it normally would.
        if (clickAction == ClickAction.PRIMARY && ColorPaletteContents.isDye(other))
        {
            InsertResult result = check(self, other);
            if (result != InsertResult.REJECTED)
            {
                // Take it out of the slot first: safeTake respects slots you can't take from (e.g. crafting
                // results), and gives back EMPTY then
                ItemStack taken = slot.safeTake(1, 1, player);
                if (taken.isEmpty()) result = InsertResult.REJECTED;
                else insert(self, taken);
            }
            playInsertSound(player, result);
            slotsChanged(player);
            return true;
        }
        // Right-click an empty slot: the selected dye goes into it
        if (clickAction == ClickAction.SECONDARY && other.isEmpty())
        {
            ItemStack removed = extractSelected(self);
            if (!removed.isEmpty())
            {
                ItemStack left = slot.safeInsert(removed);
                if (!left.isEmpty()) insert(self, left); // the slot didn't take it: put it back
                playRemoveSound(player);
            }
            slotsChanged(player);
            return true;
        }
        return false;
    }

    // Holding something (or nothing) on the cursor, clicking the palette
    @Override
    public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction clickAction,
                                            Player player, SlotAccess carriedItem)
    {
        if (self.getCount() != 1) return false;

        // Left-click with a dye: one goes in (the rest stays on the cursor)
        if (clickAction == ClickAction.PRIMARY && ColorPaletteContents.isDye(other))
        {
            if (slot.allowModification(player))
            {
                InsertResult result = insert(self, other);
                if (result != InsertResult.REJECTED) other.shrink(1);
                playInsertSound(player, result);
            }
            slotsChanged(player);
            return true;
        }
        // Right-click with an empty cursor: the selected dye comes out onto the cursor
        if (clickAction == ClickAction.SECONDARY && other.isEmpty())
        {
            if (slot.allowModification(player))
            {
                ItemStack removed = extractSelected(self);
                if (!removed.isEmpty())
                {
                    carriedItem.set(removed);
                    playRemoveSound(player);
                }
            }
            slotsChanged(player);
            return true;
        }
        // Anything else (e.g. left-click with an empty cursor picks the palette up as normal)
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

    // ---- Sounds and syncing ----

    private static void playInsertSound(Player player, InsertResult result)
    {
        switch (result)
        {
            case STORED -> player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
            // Lower pitch: it went in, but was thrown away
            case DUPLICATE -> player.playSound(SoundEvents.BUNDLE_INSERT, 0.8f, 0.5f);
            case REJECTED -> player.playSound(SoundEvents.BUNDLE_INSERT_FAIL, 1f, 1f);
        }
    }

    private static void playRemoveSound(Player player)
    {
        player.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8f, 0.8f + player.level().getRandom().nextFloat() * 0.4f);
    }

    // Tells the open container the item changed, so the change is sent to the client
    private static void slotsChanged(Player player)
    {
        player.containerMenu.slotsChanged(player.getInventory());
    }
}
