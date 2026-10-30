package com.mowmaster.mowlib.util;

import com.mojang.authlib.GameProfile;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

import java.util.UUID;

// Who owns something (an upgrade, a machine...): the player's id and name. A fake player made with this profile
// (MowLibFakePlayer) is treated by claim/protection mods like that player.
// Saved as {"id": <uuid>, "name": "..."} (same format as Pedestals' old UpgradeOwner).
public record MowLibOwner(UUID id, String name)
{
    public static final Codec<MowLibOwner> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            UUIDUtil.CODEC.fieldOf("id").forGetter(MowLibOwner::id),
            Codec.STRING.fieldOf("name").forGetter(MowLibOwner::name))
            .apply(instance, MowLibOwner::new));

    public static final StreamCodec<ByteBuf, MowLibOwner> STREAM_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC, MowLibOwner::id,
            ByteBufCodecs.STRING_UTF8, MowLibOwner::name,
            MowLibOwner::new);

    public static MowLibOwner of(Player player)
    {
        return new MowLibOwner(player.getUUID(), player.getName().getString());
    }

    public GameProfile profile()
    {
        return new GameProfile(id, name);
    }
}
