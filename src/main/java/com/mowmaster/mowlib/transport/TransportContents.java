package com.mowmaster.mowlib.transport;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;
import net.neoforged.neoforge.transfer.resource.ResourceStack;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;

import java.util.ArrayList;
import java.util.List;

// Resources of one transport type with amounts (NeoForge's ResourceStack record): what an item that carries a
// storage's contents holds (Pedestals' storage packets), or anything else that saves a storage's contents.
// Generic like the transport types: TransportContents<FluidResource> holds fluids, and so on. A component of type
// TransportContents<?> ("some type") works for every type.
//
// Saved as {"type": "mowlib:fluids", "contents": [{"resource": {...}, "amount": 16000}]}: "dispatch" reads the
// type first, then uses that type's resource codec for the rest. Types without a resource codec can't be saved.
public record TransportContents<R extends Resource>(TransportType<R> type, List<ResourceStack<R>> contents)
{
    public static final Codec<TransportContents<?>> CODEC = TransportTypes.REGISTRY.byNameCodec()
            .dispatch("type", TransportContents::type, type -> codecFor(type));

    // Through the codec: simple, and these are rarely sent
    public static final StreamCodec<RegistryFriendlyByteBuf, TransportContents<?>> STREAM_CODEC = ByteBufCodecs.fromCodecWithRegistries(CODEC);

    public TransportContents
    {
        contents = List.copyOf(contents);
    }

    public static <R extends Resource> TransportContents<R> empty(TransportType<R> type)
    {
        return new TransportContents<>(type, List.of());
    }

    private static <R extends Resource> MapCodec<TransportContents<R>> codecFor(TransportType<R> type)
    {
        Codec<R> resourceCodec = type.getResourceCodec();
        if (resourceCodec == null) throw new IllegalStateException("Transport type " + type.getId() + " has no resource codec, so it can't be saved");
        Codec<ResourceStack<R>> stackCodec = RecordCodecBuilder.create(instance -> instance.group(
                resourceCodec.fieldOf("resource").forGetter(ResourceStack::resource),
                Codec.INT.fieldOf("amount").forGetter(ResourceStack::amount))
                .apply(instance, ResourceStack::new));
        return stackCodec.listOf().fieldOf("contents").xmap(list -> new TransportContents<>(type, list), TransportContents::contents);
    }

    public boolean isEmpty()
    {
        return contents.isEmpty();
    }

    public int totalAmount()
    {
        return contents.stream().mapToInt(ResourceStack::amount).sum();
    }

    // Adds amount of resource, merged into the matching entry if there is one
    public TransportContents<R> plus(R resource, int amount)
    {
        List<ResourceStack<R>> changed = new ArrayList<>(contents);
        for (int index = 0; index < changed.size(); index++)
        {
            if (changed.get(index).resource().equals(resource))
            {
                changed.set(index, new ResourceStack<>(resource, changed.get(index).amount() + amount));
                return new TransportContents<>(type, changed);
            }
        }
        changed.add(new ResourceStack<>(resource, amount));
        return new TransportContents<>(type, changed);
    }

    // Takes everything out of a handler (as part of the transaction) and returns it
    public static <R extends Resource> TransportContents<R> extractAll(TransportType<R> type, ResourceHandler<R> handler, TransactionContext transaction)
    {
        TransportContents<R> contents = empty(type);
        for (int index = 0; index < handler.size(); index++)
        {
            R resource = handler.getResource(index);
            int amount = handler.getAmountAsInt(index);
            if (resource.isEmpty() || amount <= 0) continue;
            int extracted = handler.extract(index, resource, amount, transaction);
            if (extracted > 0) contents = contents.plus(resource, extracted);
        }
        return contents;
    }

    // Puts as much as fits into a handler; returns what's left (empty if it all went in)
    public TransportContents<R> insertInto(ResourceHandler<R> handler, TransactionContext transaction)
    {
        List<ResourceStack<R>> left = new ArrayList<>();
        for (ResourceStack<R> stack : contents)
        {
            int inserted = handler.insert(stack.resource(), stack.amount(), transaction);
            if (inserted < stack.amount()) left.add(new ResourceStack<>(stack.resource(), stack.amount() - inserted));
        }
        return new TransportContents<>(type, left);
    }
}
