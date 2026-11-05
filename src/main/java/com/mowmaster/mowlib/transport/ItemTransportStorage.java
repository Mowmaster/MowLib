package com.mowmaster.mowlib.transport;

import com.mowmaster.mowlib.registry.MowLibDataComponents;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// An ITransportStorageItem's contents, opened as a normal MowLib TransportStorage, so the same code works on the item
// (in a hand or an inventory) as on a block holding the same type. The contents live in the item's
// mowlib:transport_contents component: opening makes a storage of the item's type, fills it from the component, and
// every committed change writes the component back (this is the storage's TransportStorageHost, so it's told on each
// change).
//   ItemTransportStorage storage = ItemTransportStorage.open(stack);   // null if it isn't such an item
//   storage.handler(TransportTypes.FLUIDS.get())                        // use it like any ResourceHandler
// (Moved from Bibliomania's BookStorage.)
public final class ItemTransportStorage implements TransportStorageHost
{
    private final ItemStack stack;
    private final TransportType<?> type;
    private final int capacity;
    private final TransportStorage<?> storage;
    private boolean loading;

    private ItemTransportStorage(ItemStack stack, TransportType<?> type, int capacity)
    {
        this.stack = stack;
        this.type = type;
        this.capacity = capacity;
        this.storage = type.createStorage(this);
        loading = true;
        TransportContents<?> saved = stack.get(MowLibDataComponents.TRANSPORT_CONTENTS.get());
        if (saved != null && saved.type() == type) fill(saved, storage);
        loading = false;
    }

    // The contents of a transport storage item, or null if it isn't one
    public static @Nullable ItemTransportStorage open(ItemStack stack)
    {
        if (!(stack.getItem() instanceof ITransportStorageItem item)) return null;
        return new ItemTransportStorage(stack, item.storageType(stack), item.storageCapacity(stack));
    }

    public ItemStack stack()
    {
        return stack;
    }

    public TransportType<?> type()
    {
        return type;
    }

    public TransportStorage<?> storage()
    {
        return storage;
    }

    // The storage as a handler of a known type's resource, e.g. handler(TransportTypes.FLUIDS.get()) for fluids.
    // The cast is checked: it throws if the item holds a different type.
    @SuppressWarnings("unchecked")
    public <R extends Resource> TransportStorage<R> handler(TransportType<R> expected)
    {
        if (expected != type) throw new IllegalArgumentException("Item holds " + type.getId() + ", not " + expected.getId());
        return (TransportStorage<R>) storage;
    }

    // What it holds (slot 0: these items hold one kind at a time)
    public long amount()
    {
        return storage.getAmountAsLong(0);
    }

    public int capacity()
    {
        return capacity;
    }

    // ---- TransportStorageHost ----

    @Override
    public void onStorageChanged(TransportType<?> changed)
    {
        if (!loading) stack.set(MowLibDataComponents.TRANSPORT_CONTENTS.get(), snapshot(type, storage));
    }

    @Override
    public int getCapacity(TransportType<?> asked)
    {
        return capacity;
    }

    // ---- Shared with blocks that hold the same type (placing an item moves its contents into the block) ----

    // What a handler holds, as a TransportContents (for the item component). Reads only, changes nothing.
    // The cast is safe: a type's storage always holds that type's resource.
    @SuppressWarnings("unchecked")
    public static <R extends Resource> TransportContents<R> snapshot(TransportType<R> type, ResourceHandler<?> handler)
    {
        ResourceHandler<R> typed = (ResourceHandler<R>) handler;
        List<ResourceStack<R>> stacks = new ArrayList<>();
        for (int index = 0; index < typed.size(); index++)
        {
            R resource = typed.getResource(index);
            int amount = typed.getAmountAsInt(index);
            if (!resource.isEmpty() && amount > 0) stacks.add(new ResourceStack<>(resource, amount));
        }
        return new TransportContents<>(type, stacks);
    }

    // Puts saved contents into a storage of the same type (anything over its capacity is lost).
    // Items are often opened while another transaction is already running (a shelf's handlers open an item to read
    // it in the middle of an insert or extract), and only one root transaction may be open at a time: then this one is
    // opened inside the running one instead (Transaction.open(parent)). The filled storage is only a fresh view of the
    // item, so it doesn't matter that its filling is undone if the outer transaction is.
    @SuppressWarnings({"unchecked", "deprecation"})
    public static <R extends Resource> void fill(TransportContents<R> contents, TransportStorage<?> storage)
    {
        // getCurrentOpenedTransaction: the innermost open transaction on this thread (deprecated only as a hint to pass
        // transactions along where possible; here the caller can't). null: none open, so this one is the root.
        TransactionContext running = Transaction.getLifecycle() == Transaction.Lifecycle.OPEN ? Transaction.getCurrentOpenedTransaction() : null;
        try (Transaction transaction = Transaction.open(running))
        {
            contents.insertInto((TransportStorage<R>) storage, transaction);
            transaction.commit();
        }
    }
}
