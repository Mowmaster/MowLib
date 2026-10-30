package com.mowmaster.mowlib.util;

import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

// The "player" a machine acts as when it breaks, places, uses or attacks (replaces 1.20's MowLibFakePlayer).
// It uses the owner's id and name, so protection mods treat it like that player; with no owner, the mod's own
// fallback profile (see profile(String)).
// NeoForge's FakePlayer never ticks, so two things a normal player gets from ticking are done here instead:
// attacks are always fully charged, and the held tool's attributes (attack damage, mining speed...) are applied
// whenever the held item is set with holdTool.
public class MowLibFakePlayer extends FakePlayer
{
    private ItemStack heldTool = ItemStack.EMPTY;

    public MowLibFakePlayer(ServerLevel level, @Nullable MowLibOwner owner, GameProfile fallback)
    {
        super(level, owner == null ? fallback : owner.profile());
    }

    // A fixed profile for a mod's machines when they have no owner, e.g. profile("[Pedestals]").
    // The id comes from the name, so it's the same every time.
    public static GameProfile profile(String name)
    {
        return new GameProfile(UUID.nameUUIDFromBytes(name.getBytes(StandardCharsets.UTF_8)), name);
    }

    // Stands it at the centre of the block
    public void standOn(BlockPos pos)
    {
        snapTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
    }

    // Puts the tool in the main hand and swaps the old tool's attribute modifiers for the new one's.
    // Pass the machine's own stack to have it worn down, or a copy to leave it untouched.
    public void holdTool(ItemStack tool)
    {
        // Each tool modifier is applied under its own id, so removing by id takes exactly the old ones off
        heldTool.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = getAttributes().getInstance(attribute);
            if (instance != null) instance.removeModifier(modifier.id());
        });
        setItemInHand(InteractionHand.MAIN_HAND, tool);
        tool.forEachModifier(EquipmentSlot.MAINHAND, (attribute, modifier) -> {
            AttributeInstance instance = getAttributes().getInstance(attribute);
            if (instance != null) instance.addOrUpdateTransientModifier(modifier);
        });
        heldTool = tool;
    }

    // Every attack is a full-strength one (a ticking player recharges between swings)
    @Override
    public float getAttackStrengthScale(float partialTicks)
    {
        return 1.0F;
    }
}
