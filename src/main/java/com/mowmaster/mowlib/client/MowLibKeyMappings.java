package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.mowlib.MowLibIds;
import net.minecraft.client.KeyMapping;

// MowLib's configurable keys (Options > Controls > Key Binds, under "MowLib"). Registered in MowLibClient.
// Since 1.21.9 a key's category is a KeyMapping.Category (a namespaced id) instead of a string; a custom one must be
// registered too (RegisterKeyMappingsEvent#registerCategory). Since 26.3 key codes are SDL scancodes: always use
// the InputConstants.KEY_* constants rather than raw numbers.
public final class MowLibKeyMappings
{
    private MowLibKeyMappings() {}

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(MowLibIds.id("mowlib"));

    // Turn to the next / previous page of a paged item (hovered in an inventory, or held) or block (looked at)
    public static final KeyMapping PAGE_NEXT = new KeyMapping("key.mowlib.page_next", InputConstants.KEY_PAGEDOWN, CATEGORY);
    public static final KeyMapping PAGE_PREVIOUS = new KeyMapping("key.mowlib.page_previous", InputConstants.KEY_PAGEUP, CATEGORY);
}
