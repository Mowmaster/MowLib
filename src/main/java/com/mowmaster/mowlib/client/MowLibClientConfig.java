package com.mowmaster.mowlib.client;

import net.neoforged.neoforge.common.ModConfigSpec;

// MowLib's settings for this player's screen only (ModConfig.Type.CLIENT: config/mowlib-client.toml, never sent to
// servers), registered in MowLibClient. Editable in game: Mods > MowLib > Config.
// Where the reading HUD's two panels go (client/reading/ReadingHud), for every mod using it:
// - [reading_paper]  the paper with a block's or item's details (default: the middle of the left edge)
// - [reading_label]  the small label naming any other block or mob (default: the top middle)
// Each is pinned to a screen anchor, then moved by the offsets (positive x = right, positive y = down).
public final class MowLibClientConfig
{
    private MowLibClientConfig() {}

    private static final ModConfigSpec.Builder BUILDER = new ModConfigSpec.Builder();

    public static final ModConfigSpec.BooleanValue PAPER_ENABLED;
    public static final ModConfigSpec.EnumValue<HudAnchor> PAPER_ANCHOR;
    public static final ModConfigSpec.IntValue PAPER_X;
    public static final ModConfigSpec.IntValue PAPER_Y;
    public static final ModConfigSpec.IntValue PAPER_MAX_WIDTH;
    public static final ModConfigSpec.BooleanValue PAPER_HELD;
    public static final ModConfigSpec.BooleanValue LABEL_ENABLED;
    public static final ModConfigSpec.EnumValue<HudAnchor> LABEL_ANCHOR;
    public static final ModConfigSpec.IntValue LABEL_X;
    public static final ModConfigSpec.IntValue LABEL_Y;
    public static final ModConfigSpec.BooleanValue LABEL_AMOUNTS;

    // A static block: runs once when the class loads, in order, so each push/pop wraps the values between them
    static
    {
        BUILDER.comment("The paper showing a block's or item's details while you can read (a helmet in #mowlib:grants_reading)").push("reading_paper");
        PAPER_ENABLED = BUILDER.define("enabled", true);
        PAPER_ANCHOR = BUILDER.comment("Where on the screen it's pinned").defineEnum("anchor", HudAnchor.MIDDLE_LEFT);
        PAPER_X = BUILDER.comment("Moved this far right from there (negative: left)").defineInRange("x_offset", 0, -4000, 4000);
        PAPER_Y = BUILDER.comment("Moved this far down from there (negative: up)").defineInRange("y_offset", 0, -4000, 4000);
        PAPER_MAX_WIDTH = BUILDER.comment("Longer lines wrap at this width").defineInRange("max_width", 180, 60, 600);
        PAPER_HELD = BUILDER.comment("Also show a readable item held in the hand (when not looking at a readable block); turning",
                "its pages then shows here instead of above the hotbar").define("held_items", true);
        BUILDER.pop();

        BUILDER.comment("The small label naming any other block or mob while you can read").push("reading_label");
        LABEL_ENABLED = BUILDER.define("enabled", true);
        LABEL_ANCHOR = BUILDER.comment("Where on the screen it's pinned").defineEnum("anchor", HudAnchor.TOP_CENTER);
        LABEL_X = BUILDER.comment("Moved this far right from there (negative: left)").defineInRange("x_offset", 0, -4000, 4000);
        LABEL_Y = BUILDER.comment("Moved this far down from there (negative: up)").defineInRange("y_offset", 0, -4000, 4000);
        LABEL_AMOUNTS = BUILDER.comment("Also show the energy, fluids and experience a block holds").define("show_amounts", true);
        BUILDER.pop();
    }

    public static final ModConfigSpec SPEC = BUILDER.build();
}
