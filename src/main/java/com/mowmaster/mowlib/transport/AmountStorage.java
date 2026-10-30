package com.mowmaster.mowlib.transport;

import com.mowmaster.mowlib.filter.FilterDirection;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.TransferPreconditions;
import net.neoforged.neoforge.transfer.transaction.SnapshotJournal;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

// A storage that only holds an amount (energy, experience, or another mod's amount-only type), with transaction
// support and the host's filter applied. The capacity comes from the host (TransportStorageHost#getCapacity).
// Subclass it to change what happens when the holder is removed (dropContents) or how it's described.
public class AmountStorage implements TransportStorage<AmountResource>
{
    protected final TransportStorageHost host;
    protected final TransportType<AmountResource> type;
    protected final AmountResource resource;
    protected int amount;

    // Transactions: before the first change inside a transaction this remembers the old amount, so a rollback
    // (transaction closed without commit) can put it back. Same pattern as NeoForge's SimpleEnergyHandler.
    private final SnapshotJournal<Integer> journal = new SnapshotJournal<>()
    {
        @Override
        protected Integer createSnapshot()
        {
            return amount;
        }

        @Override
        protected void revertToSnapshot(Integer snapshot)
        {
            amount = snapshot;
        }

        @Override
        protected void onRootCommit(Integer originalState)
        {
            if (amount != originalState) host.onStorageChanged(type);
        }
    };

    public AmountStorage(TransportStorageHost host, TransportType<AmountResource> type, AmountResource resource)
    {
        this.host = host;
        this.type = type;
        this.resource = resource;
    }

    // Before the filter's limit
    public int getCapacity()
    {
        return Math.max(0, host.getCapacity(type));
    }

    public int getAmount()
    {
        return amount;
    }

    // The capacity the host's filter allows
    protected int getAllowedCapacity()
    {
        return host.filterLimit(type, resource, getCapacity());
    }

    // ---- ResourceHandler: one index, holding this type's single resource ----

    @Override
    public int size()
    {
        return 1;
    }

    @Override
    public AmountResource getResource(int index)
    {
        return amount > 0 ? resource : AmountResource.EMPTY;
    }

    @Override
    public long getAmountAsLong(int index)
    {
        return amount;
    }

    @Override
    public long getCapacityAsLong(int index, AmountResource resource)
    {
        return getAllowedCapacity();
    }

    @Override
    public boolean isValid(int index, AmountResource resource)
    {
        return this.resource.equals(resource) && host.filterAllows(type, resource, FilterDirection.INSERT);
    }

    @Override
    public int insert(int index, AmountResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!isValid(index, resource)) return 0;

        int inserted = Math.min(amount, Math.max(0, getAllowedCapacity() - this.amount));
        if (inserted > 0)
        {
            journal.updateSnapshots(transaction);
            this.amount += inserted;
        }
        return inserted;
    }

    @Override
    public int extract(int index, AmountResource resource, int amount, TransactionContext transaction)
    {
        TransferPreconditions.checkNonEmptyNonNegative(resource, amount);
        if (!this.resource.equals(resource) || !host.filterAllows(type, resource, FilterDirection.EXTRACT)) return 0;

        int extracted = Math.min(amount, this.amount);
        if (extracted > 0)
        {
            journal.updateSnapshots(transaction);
            this.amount -= extracted;
        }
        return extracted;
    }

    // "Energy: 500/10000"
    @Override
    public @Nullable Component describe()
    {
        if (amount <= 0) return null;
        return Component.empty().append(type.getName()).append(": " + amount + "/" + getAllowedCapacity()).withStyle(type.getColor());
    }

    // ---- Saving ----

    @Override
    public void serialize(ValueOutput output)
    {
        if (amount > 0) output.putInt("amount", amount);
    }

    @Override
    public void deserialize(ValueInput input)
    {
        amount = Math.max(0, input.getIntOr("amount", 0));
    }
}
