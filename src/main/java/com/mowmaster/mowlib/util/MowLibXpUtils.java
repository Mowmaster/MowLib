package com.mowmaster.mowlib.util;

import net.minecraft.world.entity.player.Player;

// Experience maths: vanilla's levelling formula, and reading/setting a player's real total points.
// Player#totalExperience can't be trusted for that: vanilla only raises it in giveExperiencePoints, so levels from
// commands, or levels spent at an enchanting table or anvil, never show up in it.
public final class MowLibXpUtils
{
    private MowLibXpUtils()
    {
    }

    // Total points to get from level 0 to this level
    public static int pointsForLevel(int level)
    {
        if (level <= 0) return 0;
        long points;
        if (level <= 16) points = (long) level * level + 6L * level;
        else if (level <= 31) points = (long) (2.5 * level * level - 40.5 * level + 360);
        else points = (long) (4.5 * level * level - 162.5 * level + 2220);
        return (int) Math.min(points, Integer.MAX_VALUE);
    }

    // The highest level whose total is at most this many points
    public static int levelForPoints(int points)
    {
        int level = 0;
        while (pointsForLevel(level + 1) <= points) level++;
        return level;
    }

    // The player's real total points, from their level and progress bar
    public static int getPlayerExperience(Player player)
    {
        long points = pointsForLevel(player.experienceLevel)
                + Math.round(player.experienceProgress * player.getXpNeededForNextLevel());
        return (int) Math.min(points, Integer.MAX_VALUE);
    }

    // Sets the player to exactly this many points (level + progress bar), all at once
    public static void setPlayerExperience(Player player, int points)
    {
        points = Math.max(0, points);
        int level = levelForPoints(points);
        player.experienceLevel = level;
        // getXpNeededForNextLevel reads experienceLevel, so it's set first
        player.experienceProgress = (float) (points - pointsForLevel(level)) / player.getXpNeededForNextLevel();
        player.totalExperience = points;
        // Adding 0 levels changes nothing, but on the server it makes the player resend their xp bar to the client
        player.giveExperienceLevels(0);
    }

    // Takes up to amount points from the player; returns how many were taken
    public static int takePlayerExperience(Player player, int amount)
    {
        int current = getPlayerExperience(player);
        int taken = Math.min(Math.max(0, amount), current);
        if (taken > 0) setPlayerExperience(player, current - taken);
        return taken;
    }
}
