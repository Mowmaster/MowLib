package com.mowmaster.mowlib.reading;

import com.mowmaster.mowlib.transport.TransportType;
import com.mowmaster.mowlib.transport.TransportTypes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AgeableMob;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.ChiseledBookShelfBlock;
import net.minecraft.world.level.block.entity.ChiseledBookShelfBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.ArrayList;
import java.util.List;

// The small label's extra lines (server): what any block or mob holds that a client usually doesn't know.
// - blocks: on a chiseled bookshelf the looked-at book's tooltip; then every non-item MowLib TransportType the block
//   offers (energy, fluids, experience, and any a mod adds): "Energy: 4000 / 10000", "Water: 2000mB / 16000"
//   (items are left out: a chest would list every slot); then ReadingRegistry's block label lines
// - mobs: their effects (level and time left), how long until a baby grows up or an adult can breed again; then
//   ReadingRegistry's entity label lines
public final class ReadingLabels
{
    private ReadingLabels() {}

    public static List<Component> describeBlock(ServerLevel level, BlockPos pos, int slot, Player player)
    {
        List<Component> lines = new ArrayList<>();
        if (slot >= 0 && slot < 6 && level.getBlockEntity(pos) instanceof ChiseledBookShelfBlockEntity shelf)
        {
            ItemStack book = shelf.getItem(slot);
            if (!book.isEmpty()) lines.addAll(ReadingPapers.tooltip(book, level, player));
        }
        for (TransportType<?> type : TransportTypes.REGISTRY)
        {
            if (type == TransportTypes.ITEMS.get()) continue;
            describeType(type, level, pos, lines);
            if (lines.size() >= ReadingPayloads.MAX_LINES) break;
        }
        ReadingRegistry.addBlockLabelLines(level, pos, player, lines);
        return cap(lines);
    }

    // <R extends Resource>: a generic method, so the handler's resources and the type's describeAmount agree
    private static <R extends Resource> void describeType(TransportType<R> type, ServerLevel level, BlockPos pos, List<Component> lines)
    {
        ResourceHandler<R> handler = type.getBlockHandler(level, pos, null);
        if (handler == null) return;
        for (int index = 0; index < handler.size() && lines.size() < ReadingPayloads.MAX_LINES; index++)
        {
            R resource = handler.getResource(index);
            long amount = handler.getAmountAsLong(index);
            long capacity = handler.getCapacityAsLong(index, resource);
            if (capacity <= 0 && amount <= 0) continue;
            // "Water: 2000 mB / 16000 mB", or for an empty slot "Fluids: 0 mB / 16000 mB": the type's own wording
            lines.add(type.describeStored(resource, amount, capacity));
        }
    }

    public static List<Component> describeEntity(Entity entity, Player player)
    {
        List<Component> lines = new ArrayList<>();
        // Effects first: "Speed II 1:30" in blue (good) or red (bad), like a potion's tooltip
        if (entity instanceof LivingEntity living)
        {
            for (MobEffectInstance instance : living.getActiveEffects())
            {
                if (lines.size() >= ReadingPayloads.MAX_LINES - 1) break;
                lines.add(effect(instance));
            }
        }
        if (entity instanceof AgeableMob mob)
        {
            // getAge: negative while a baby (ticks left to grow up), positive after breeding (ticks to wait), 0 ready
            int age = mob.getAge();
            if (age < 0) lines.add(ReadingPapers.hud("grows_up", time(-age)).withStyle(ChatFormatting.DARK_GREEN));
            else if (age > 0) lines.add(ReadingPapers.hud("breed_cooldown", time(age)).withStyle(ChatFormatting.GOLD));
            else if (mob instanceof Animal animal)
            {
                lines.add(animal.isInLove() ? ReadingPapers.hud("in_love").withStyle(ChatFormatting.LIGHT_PURPLE)
                        : ReadingPapers.hud("can_breed").withStyle(ChatFormatting.DARK_GREEN));
            }
        }
        ReadingRegistry.addEntityLabelLines(entity, player, lines);
        return cap(lines);
    }

    private static Component effect(MobEffectInstance instance)
    {
        Holder<MobEffect> effect = instance.getEffect();
        MutableComponent line = Component.translatable(effect.value().getDescriptionId());
        // Amplifier 0 is level I: "enchantment.level.2" is vanilla's "II"
        if (instance.getAmplifier() > 0) line.append(" ").append(Component.translatable("enchantment.level." + (instance.getAmplifier() + 1)));
        line.append(" " + (instance.isInfiniteDuration() ? "∞" : time(instance.getDuration())));
        // getTooltipFormatting: blue for beneficial, red for harmful, like potion tooltips
        return line.withStyle(effect.value().getCategory().getTooltipFormatting());
    }

    // Ticks as minutes:seconds
    public static String time(int ticks)
    {
        int seconds = (ticks + 19) / 20;
        return String.format("%d:%02d", seconds / 60, seconds % 60);
    }

    private static List<Component> cap(List<Component> lines)
    {
        return lines.size() > ReadingPayloads.MAX_LINES ? lines.subList(0, ReadingPayloads.MAX_LINES) : lines;
    }

    // Which of a chiseled bookshelf's six slots the crosshair is on (0-2 the top row from the left, 3-5 the bottom), or
    // -1 (not a chiseled bookshelf, or not its front). The same sums as vanilla's ChiseledBookShelfBlock. Client side.
    public static int bookshelfSlot(BlockState state, BlockHitResult hit)
    {
        if (!(state.getBlock() instanceof ChiseledBookShelfBlock) || !state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)) return -1;
        Direction face = hit.getDirection();
        if (face != state.getValue(BlockStateProperties.HORIZONTAL_FACING)) return -1;
        BlockPos pos = hit.getBlockPos();
        Vec3 at = hit.getLocation().subtract(pos.getX(), pos.getY(), pos.getZ());
        double across = switch (face)
        {
            case NORTH -> 1.0 - at.x();
            case SOUTH -> at.x();
            case WEST -> at.z();
            case EAST -> 1.0 - at.z();
            default -> -1;
        };
        if (across < 0) return -1;
        int column = across < 0.375 ? 0 : across < 0.6875 ? 1 : 2;
        int row = at.y() >= 0.5 ? 0 : 1;
        return column + row * 3;
    }
}
