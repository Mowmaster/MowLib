package com.mowmaster.mowlib.display;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.MowLibReferences;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.tags.TagKey;
import net.minecraft.util.StringRepresentable;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.DyeColor;

import java.util.Locale;
import java.util.Optional;

// How to show a mob as a display (on a pedestal, in a cage, as a recipe preview...): which mob, which way it faces,
// baby / adult / growing up, and for sheep the wool colour and whether it's sheared. Drawn by
// client/MobDisplayRenderer (inside your own block entity renderer) or client/MobDisplayBlockRenderer (a ready-made
// renderer for IMobDisplayHolder block entities).
//
// This is common code: a block entity can keep one, save it with CODEC and sync it with STREAM_CODEC, the same as
// GhostItem. It's a record, so it's immutable: the with... methods return a changed copy, e.g.
//     MobDisplay.of(EntityType.SHEEP).facingPlayer().sheep(DyeColor.LIME, true).growingUp()
//
// type:       the mob. Mobs in #mowlib:mob_display_blacklist (ender dragon, player...) are never drawn.
// facing:     SPIN (turning slowly), FACE_PLAYER (turned towards the camera), FIXED (turned `yaw` degrees,
//             0 = facing south, 90 = west, like an entity's own yaw)
// age:        ADULT, BABY, or GROWING (loops: baby, grows, a puff of particles, adult). Mobs without babies always
//             show as adults.
// sheepColor: wool colour for sheep (empty = white); sheared: shows a sheared sheep. Ignored for other mobs.
// size:       the largest the mob may be, in blocks. Bigger mobs are shrunk to fit; smaller ones keep their size.
public record MobDisplay(EntityType<?> type, Facing facing, float yaw, Age age, Optional<DyeColor> sheepColor, boolean sheared, float size)
{
    // Entity types that can't be drawn this way (they need a player profile, or are made of several parts).
    // data/mowlib/tags/entity_type/mob_display_blacklist.json; mods and datapacks can add to it.
    public static final TagKey<EntityType<?>> BLACKLIST = TagKey.create(Registries.ENTITY_TYPE, MowLibReferences.id("mob_display_blacklist"));

    // StringRepresentable.fromEnum reads and writes an enum by its getSerializedName ("spin", "face_player"...)
    public enum Facing implements StringRepresentable
    {
        SPIN, FACE_PLAYER, FIXED;

        public static final Codec<Facing> CODEC = StringRepresentable.fromEnum(Facing::values);

        @Override
        public String getSerializedName()
        {
            return name().toLowerCase(Locale.ROOT); // Locale.ROOT: the same on every computer
        }
    }

    public enum Age implements StringRepresentable
    {
        ADULT, BABY, GROWING;

        public static final Codec<Age> CODEC = StringRepresentable.fromEnum(Age::values);

        @Override
        public String getSerializedName()
        {
            return name().toLowerCase(Locale.ROOT); // Locale.ROOT: the same on every computer
        }
    }

    // Saved as {"type": "minecraft:sheep", "facing": "spin", "age": "growing", "sheep_color": "lime", ...};
    // everything but "type" is optional
    public static final Codec<MobDisplay> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    BuiltInRegistries.ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(MobDisplay::type),
                    Facing.CODEC.optionalFieldOf("facing", Facing.SPIN).forGetter(MobDisplay::facing),
                    Codec.FLOAT.optionalFieldOf("yaw", 0f).forGetter(MobDisplay::yaw),
                    Age.CODEC.optionalFieldOf("age", Age.ADULT).forGetter(MobDisplay::age),
                    DyeColor.CODEC.optionalFieldOf("sheep_color").forGetter(MobDisplay::sheepColor),
                    Codec.BOOL.optionalFieldOf("sheared", false).forGetter(MobDisplay::sheared),
                    Codec.FLOAT.optionalFieldOf("size", 1f).forGetter(MobDisplay::size))
            .apply(instance, MobDisplay::new));

    // For block entity update packets or your own payloads (through the codec: simple, and rarely sent)
    public static final StreamCodec<ByteBuf, MobDisplay> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    // An adult, spinning, at most 1 block big
    public static MobDisplay of(EntityType<?> type)
    {
        return new MobDisplay(type, Facing.SPIN, 0f, Age.ADULT, Optional.empty(), false, 1f);
    }

    // Whether this type may be shown at all (it still has to be a living entity when it's made on the client)
    public static boolean canDisplay(EntityType<?> type)
    {
        return !type.builtInRegistryHolder().is(BLACKLIST);
    }

    public MobDisplay spinning()
    {
        return new MobDisplay(type, Facing.SPIN, yaw, age, sheepColor, sheared, size);
    }

    public MobDisplay facingPlayer()
    {
        return new MobDisplay(type, Facing.FACE_PLAYER, yaw, age, sheepColor, sheared, size);
    }

    public MobDisplay fixed(float newYaw)
    {
        return new MobDisplay(type, Facing.FIXED, newYaw, age, sheepColor, sheared, size);
    }

    public MobDisplay adult()
    {
        return new MobDisplay(type, facing, yaw, Age.ADULT, sheepColor, sheared, size);
    }

    public MobDisplay baby()
    {
        return new MobDisplay(type, facing, yaw, Age.BABY, sheepColor, sheared, size);
    }

    public MobDisplay growingUp()
    {
        return new MobDisplay(type, facing, yaw, Age.GROWING, sheepColor, sheared, size);
    }

    // Optional.ofNullable: empty when color is null (white wool)
    public MobDisplay sheep(DyeColor color, boolean isSheared)
    {
        return new MobDisplay(type, facing, yaw, age, Optional.ofNullable(color), isSheared, size);
    }

    public MobDisplay withSize(float newSize)
    {
        return new MobDisplay(type, facing, yaw, age, sheepColor, sheared, newSize);
    }
}
