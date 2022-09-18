package com.mowmaster.mowlib.MowLibUtils;

import com.mojang.authlib.GameProfile;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;
import java.util.UUID;

public class MowLibFakePlayer extends FakePlayer {
    private static WeakReference<MowLibFakePlayer> INSTANCE;
    private BlockPos fakePos;
    private ItemStack heldItem;

    public MowLibFakePlayer(ServerLevel world, @Nullable UUID getPlayerUUID, @Nullable String getPlayerName, @Nullable BlockPos setPos, @Nullable ItemStack toolHeld, String defaultName) {
        super(world, new GameProfile((getPlayerUUID != null)?(getPlayerUUID):(Util.NIL_UUID),(getPlayerName != null)?(getPlayerName):(defaultName)));
        this.fakePos = (setPos !=null)?(setPos):(BlockPos.ZERO);
        this.setItemInHand(InteractionHand.MAIN_HAND,toolHeld);
    }

    //Set all sounds to silent???
    //Thanks again Loth: https://github.com/Lothrazar/Cyclic/blob/4ce8b97b8851d207af7712425f9f58506829583e/src/main/java/com/lothrazar/cyclic/util/UtilFakePlayer.java#L66
    //Didnt know that was a thing :D
    @Override
    public void setSilent(boolean isSilent) {
        super.setSilent(true);
    }

    @Override
    public boolean isAffectedByPotions() {
        return false;
    }

    public void setToolInHand(MowLibFakePlayer player, InteractionHand hand, ItemStack getTool)
    {
        player.setItemInHand(hand,getTool);
    }


}
