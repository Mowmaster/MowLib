package com.mowmaster.mowlib.transport;

import com.mowmaster.mowlib.storage.StorageInteractions;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.access.ItemAccess;

// Bucket-style inventory clicks and a fill bar for any ITransportStorageItem, working with any item that holds the
// same type (found through TransportType#getItemHandler: a bucket for fluids, a battery for energy...):
// - left-click: fills the storage item from the other item; right-click: fills the other item from the storage item
// - either way round: the storage item on the cursor clicked onto the other in a slot, or the other on the cursor
//   clicked onto the storage item
// An interface can't override Item's methods, so the item overrides these and calls in here (an item with special
// clicks of its own checks those first and falls back on these):
//
//     @Override public boolean overrideStackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player)
//     { return TransportItemInteractions.stackedOnOther(self, slot, action, player); }
//     @Override public boolean overrideOtherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action,
//                                                       Player player, SlotAccess carried)
//     { return TransportItemInteractions.otherStackedOnMe(self, other, slot, action, player); }
//     @Override public boolean isBarVisible(ItemStack stack) { return TransportItemInteractions.barVisible(stack); }
//     @Override public int getBarWidth(ItemStack stack) { return TransportItemInteractions.barWidth(stack); }
//     @Override public int getBarColor(ItemStack stack) { return TransportItemInteractions.barColor(stack); }
//
// The clicks answer the same on both sides; only the server moves anything. (Moved from Bibliomania's storage books.)
public final class TransportItemInteractions
{
    private TransportItemInteractions() {}

    // Something on the cursor clicked onto the storage item in a slot
    public static boolean otherStackedOnMe(ItemStack self, ItemStack other, Slot slot, ClickAction action, Player player)
    {
        if (self.getCount() != 1 || other.isEmpty() || !slot.allowModification(player)) return false;
        ItemTransportStorage storage = ItemTransportStorage.open(self);
        return storage != null && useContainer(storage.type(), storage.storage(), StorageInteractions.cursorAccess(player),
                action == ClickAction.SECONDARY, player.level().isClientSide());
    }

    // The storage item on the cursor clicked onto the item in a slot
    public static boolean stackedOnOther(ItemStack self, Slot slot, ClickAction action, Player player)
    {
        if (self.getCount() != 1 || slot.getItem().isEmpty() || !slot.allowModification(player)) return false;
        ItemTransportStorage storage = ItemTransportStorage.open(self);
        if (storage == null) return false;
        // The client only says whether it would work (the other item holds this type), so both sides agree
        if (player.level().isClientSide()) return storage.type().getItemHandler(ItemAccess.forStack(slot.getItem().copy())) != null;
        return StorageInteractions.withSlotAccess(player, slot, access -> useContainer(storage.type(), storage.storage(), access,
                action == ClickAction.SECONDARY, false));
    }

    // An item that holds `type` (reached through `item`) used with a storage of that type: whether it holds the type at
    // all; on the server also moves everything that fits, into the item (toItem) or out of it into the storage. Also
    // for blocks holding a type (a right-click with a battery...).
    public static boolean useContainer(TransportType<?> type, ResourceHandler<?> storage, ItemAccess item, boolean toItem, boolean clientSide)
    {
        ResourceHandler<?> itemHandler = type.getItemHandler(item);
        if (itemHandler == null) return false;
        if (!clientSide)
        {
            if (toItem) StorageInteractions.move(type, storage, itemHandler, Integer.MAX_VALUE);
            else StorageInteractions.move(type, itemHandler, storage, Integer.MAX_VALUE);
        }
        return true;
    }

    // ---- The fill bar (like a durability bar, in the type's colour) ----

    public static boolean barVisible(ItemStack stack)
    {
        ItemTransportStorage storage = ItemTransportStorage.open(stack);
        return storage != null && storage.amount() > 0;
    }

    // 0 to 13 pixels wide
    public static int barWidth(ItemStack stack)
    {
        ItemTransportStorage storage = ItemTransportStorage.open(stack);
        if (storage == null || storage.capacity() <= 0) return 0;
        return Mth.clamp(Math.round(13f * storage.amount() / storage.capacity()), 0, 13);
    }

    public static int barColor(ItemStack stack)
    {
        return stack.getItem() instanceof ITransportStorageItem item ? item.storageType(stack).getRgb() : 0xFFFFFF;
    }
}
