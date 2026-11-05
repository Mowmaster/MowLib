package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.platform.InputConstants;
import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.client.KeyMapping;

// MowLib's configurable keys (Options > Controls > Key Binds, under "MowLib"). Registered in MowLibClient.
// Since 1.21.9 a key's category is a KeyMapping.Category (a namespaced id) instead of a string; a custom one must be
// registered too (RegisterKeyMappingsEvent#registerCategory). Since 26.3 key codes are SDL scancodes: always use
// the InputConstants.KEY_* constants rather than raw numbers.
public final class MowLibKeyMappings
{
    private MowLibKeyMappings() {}

    public static final KeyMapping.Category CATEGORY = new KeyMapping.Category(MowLibReferences.id("mowlib"));

    // Turn to the next / previous page of a paged item (hovered in an inventory, or held) or block (looked at)
    public static final KeyMapping PAGE_NEXT = new KeyMapping("key.mowlib.page_next", InputConstants.KEY_PAGEDOWN, CATEGORY);
    public static final KeyMapping PAGE_PREVIOUS = new KeyMapping("key.mowlib.page_previous", InputConstants.KEY_PAGEUP, CATEGORY);

    // The key names, for tooltips on items that load on servers too (Component.keybind(name) shows whatever key it's
    // bound to; the KeyMappings themselves are client only)
    public static final String ACTIVATE_NAME = "key.mowlib.activate";
    public static final String PLACE_NAME = "key.mowlib.place";
    public static final String PICKUP_NAME = "key.mowlib.pickup";

    // The action keys (client/KeyActionHandler):
    // - activate (V): the hovered item in an inventory, or the held one (keys/IActivatable)
    // - place (G): put the held block item down where you look, skipping its right-click (keys/IKeyPlaceable)
    // - pick up (H): pick up the looked-at block, contents and all (keys/IPickupableBlock)
    public static final KeyMapping ACTIVATE = new KeyMapping(ACTIVATE_NAME, InputConstants.KEY_V, CATEGORY);
    public static final KeyMapping PLACE = new KeyMapping(PLACE_NAME, InputConstants.KEY_G, CATEGORY);
    public static final KeyMapping PICKUP = new KeyMapping(PICKUP_NAME, InputConstants.KEY_H, CATEGORY);
}
