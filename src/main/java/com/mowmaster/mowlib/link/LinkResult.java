package com.mowmaster.mowlib.link;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

// What a LinkList change did, with the message the linking tool shows (message.mowlib.link.<key>).
// An enum can hold fields and methods like a class.
public enum LinkResult
{
    ADDED(true, null),
    REMOVED(true, "removed"),
    OUT_OF_RANGE(false, "out_of_range"),
    SELF(false, "self"),
    FULL(false, "full"),
    NOT_LINKABLE(false, "not_linkable");

    private final boolean changed;
    private final @Nullable String messageKey;

    LinkResult(boolean changed, @Nullable String messageKey)
    {
        this.changed = changed;
        this.messageKey = messageKey;
    }

    public boolean changed()
    {
        return changed;
    }

    // receiving: the link was made from the receiving end (the linking tool's Receive From mode)
    public Component message(boolean receiving)
    {
        String key = messageKey != null ? messageKey : receiving ? "added_receive" : "added_send";
        return Component.translatable("message." + MowLibReferences.MODID + ".link." + key).withStyle(changed ? ChatFormatting.WHITE : ChatFormatting.RED);
    }
}
