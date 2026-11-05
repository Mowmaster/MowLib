package com.mowmaster.mowlib.client;

// Nine places on the screen a HUD panel can be pinned to; a config's anchor plus x/y offsets place it. For any mod's
// HUD panels (MowLib's reading HUD, Bibliomania's Atlas map...):
//   int x = anchor.x(graphics.guiWidth(), width) + xOffset, y = anchor.y(graphics.guiHeight(), height) + yOffset;
//   x = HudAnchor.keepOnScreen(x, width, graphics.guiWidth());
public enum HudAnchor
{
    TOP_LEFT, TOP_CENTER, TOP_RIGHT,
    MIDDLE_LEFT, CENTER, MIDDLE_RIGHT,
    BOTTOM_LEFT, BOTTOM_CENTER, BOTTOM_RIGHT;

    // Gap kept from the screen's edge
    public static final int MARGIN = 4;

    // The panel's left edge for a panel this wide on a screen this wide (before offsets).
    // ordinal(): the enum's position in the list above (0-8), so % 3 is the column and / 3 the row.
    public int x(int screenWidth, int width)
    {
        return switch (ordinal() % 3)
        {
            case 0 -> MARGIN;
            case 1 -> (screenWidth - width) / 2;
            default -> screenWidth - width - MARGIN;
        };
    }

    public int y(int screenHeight, int height)
    {
        return switch (ordinal() / 3)
        {
            case 0 -> MARGIN;
            case 1 -> (screenHeight - height) / 2;
            default -> screenHeight - height - MARGIN;
        };
    }

    // Keeps a panel on screen, however far the offsets push it
    public static int keepOnScreen(int at, int size, int screen)
    {
        return Math.clamp(at, 0, Math.max(0, screen - size));
    }
}
