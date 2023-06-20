package com.mowmaster.mowlib.BlockEntities;


import com.mowmaster.mowlib.MowLibUtils.MowLibFakePlayer;
import com.mowmaster.mowlib.MowLibUtils.MowLibOwnerUtils;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraftforge.common.util.FakePlayer;

import javax.annotation.Nullable;
import java.lang.ref.WeakReference;

public class MowLibBaseBlockEntity extends BlockEntity {

    private WeakReference<FakePlayer> basePlayer;
    private MowLibBaseBlockEntity getBaseBlockEntity() { return this; }
    public BlockPos getPos() { return this.worldPosition; }

    public MowLibBaseBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    public void update()
    {

    }

    public void chatDetailsBaseBlockEntity(Player player, MowLibBaseBlockEntity baseBlockEntity)
    {

    }

    public ItemStack getWorkCard()
    {
        return ItemStack.EMPTY;
    }
    
    /*==========================================================
    ============================================================
    =====                Fake Player Start                ======
    ============================================================
    ==========================================================*/

    public WeakReference<FakePlayer> getFakePlayer()
    {
        return basePlayer;
    }

    public void setFakePlayer()
    {
        basePlayer = fakeBasePlayer(getBaseBlockEntity());
        if(basePlayer.get() != null)
        {
            basePlayer.get().setPos(getPos().getX(),getPos().getY(),getPos().getZ());
            basePlayer.get().setRespawnPosition(basePlayer.get().getRespawnDimension(), getPos(),0F,false,false);
        }
    }

    public WeakReference<FakePlayer> getBasePlayer(MowLibBaseBlockEntity baseBlockEntity) {
        if(baseBlockEntity.getFakePlayer() == null || baseBlockEntity.getFakePlayer().get() == null)
        {
            baseBlockEntity.setFakePlayer();
        }

        return baseBlockEntity.getFakePlayer();
    }

    public void updatePedestalPlayer(MowLibBaseBlockEntity baseBlockEntity)
    {
        if(baseBlockEntity.getFakePlayer() != null)
        {
            baseBlockEntity.setFakePlayer();
        }
    }

    //This will assign base params to a new fake player unless overwritten
    public WeakReference<FakePlayer> fakeBasePlayer(MowLibBaseBlockEntity baseBlockEntity)
    {
        Level level = baseBlockEntity.getLevel();
        if(level instanceof ServerLevel slevel)
        {
            return new WeakReference<FakePlayer>(new MowLibFakePlayer(slevel , null, null,getPos(),null,"[FakePlayer_"+ getPos().getX() + "x " + getPos().getY() + "y " + getPos().getZ() + "z]"));
        }
        else return null;
    }

    /*==========================================================
    ============================================================
    =====                 Fake Player End                 ======
    ============================================================
    ==========================================================*/

    public void actionOnNeighborBelowChange(BlockPos belowBlock) {}

    public static void serverTick(Level level, BlockPos blockPos, BlockState blockState, MowLibBaseBlockEntity e) {
        e.tick();
    }

    public static <E extends BlockEntity> void clientTick(Level level, BlockPos blockPos, BlockState blockState, MowLibBaseBlockEntity e) {
        e.tickClient();
    }

    public void tick() {}
    public void tickClient(){}

    @Override
    public void load(CompoundTag p_155245_) {
        super.load(p_155245_);
    }

    @Override
    protected void saveAdditional(CompoundTag p_187471_) {
        super.saveAdditional(p_187471_);
        save(p_187471_);
    }

    /*
    https://discord.com/channels/313125603924639766/915304642668290119/933514186267459658

    When you want to save some BE to something else:
- saveWithFullMetadata()if you want the full data (includes the position of the block, this may be problematic for certain applications)
- saveWithId() if you want to be able to reconstruct a BE from this data without knowing beforehand which BE type you need (for example picking up a BE with a special "carrier" item could use this)
- saveWithoutMetadata() if you only need the actual data but not the BE type or position
     */

    public CompoundTag save(CompoundTag p_58888_) {
        return p_58888_;
    }

    @Nullable
    @Override
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public CompoundTag getUpdateTag() {
        return save(new CompoundTag());
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net,pkt);
        BlockState state = this.level.getBlockState(getPos());
        this.handleUpdateTag(pkt.getTag());
        this.level.sendBlockUpdated(getPos(), state, state, 3);
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        this.load(tag);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
    }

}
