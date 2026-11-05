package com.mowmaster.mowlib.client;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;

// Things the activation key (MowLibKeyMappings.ACTIVATE) does on the CLIENT for a held item, such as opening a screen
// (the Blank Filter's radial menu). Checked before the server-side IActivatable, main hand first. Client only: register
// from your client class (MowLibClient registers the Blank Filter's).
//   ClientActivations.register(stack -> stack.is(MyItems.THING.get()), (stack, hand) -> Minecraft.getInstance().setScreen(...));
public final class ClientActivations
{
    private ClientActivations() {}

    @FunctionalInterface
    public interface Handler
    {
        void activate(ItemStack stack, InteractionHand hand);
    }

    private record Entry(Predicate<ItemStack> applies, Handler handler) {}

    // CopyOnWriteArrayList: safe to add to during setup while the game reads it
    private static final List<Entry> ENTRIES = new CopyOnWriteArrayList<>();

    public static void register(Predicate<ItemStack> applies, Handler handler)
    {
        ENTRIES.add(new Entry(applies, handler));
    }

    // Runs the first handler for this stack. true if there was one.
    public static boolean activate(ItemStack stack, InteractionHand hand)
    {
        if (stack.isEmpty()) return false;
        for (Entry entry : ENTRIES)
        {
            if (entry.applies().test(stack))
            {
                entry.handler().activate(stack, hand);
                return true;
            }
        }
        return false;
    }
}
