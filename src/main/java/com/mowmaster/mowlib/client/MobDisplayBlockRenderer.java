package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.display.IMobDisplayHolder;
import com.mowmaster.mowlib.display.MobDisplay;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Draws an IMobDisplayHolder's mob above the block (the block itself is a normal block model). Works for any mod's
// block entity type: event.registerBlockEntityRenderer(type, MobDisplayBlockRenderer::new).
// <T extends BlockEntity & IMobDisplayHolder> means T must be a block entity AND implement the interface.
// Same shape as SpinningItemRenderer; the work is done by MobDisplayRenderer.
public class MobDisplayBlockRenderer<T extends BlockEntity & IMobDisplayHolder> implements BlockEntityRenderer<T, MobDisplayBlockRenderer.State>
{
    private final MobDisplayRenderer mobs = new MobDisplayRenderer();

    public MobDisplayBlockRenderer(BlockEntityRendererProvider.Context context)
    {
    }

    @Override
    public State createRenderState()
    {
        return new State();
    }

    @Override
    public void extractRenderState(T holder, State state, float partialTick, Vec3 cameraPos,
                                   ModelFeatureRenderer.@Nullable CrumblingOverlay crumblingOverlay)
    {
        BlockEntityRenderer.super.extractRenderState(holder, state, partialTick, cameraPos, crumblingOverlay);
        MobDisplay display = holder.getMobDisplay();
        state.height = holder.getMobDisplayHeight();
        if (display == null)
        {
            state.mob.entity = null;
            return;
        }
        Vec3 feet = Vec3.atLowerCornerOf(holder.getBlockPos()).add(0.5, state.height, 0.5);
        mobs.extract(state.mob, display, holder.getLevel(), feet, cameraPos, partialTick, state.lightCoords);
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        if (state.mob.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5, state.height, 0.5);
        mobs.submit(state.mob, poseStack, submitNodeCollector, camera);
        poseStack.popPose();
    }

    // The block and the space a display-sized mob can fill above it. NeoForge skips renderers whose box is off
    // screen, so without this the mob would vanish when only the block's own space is out of view.
    @Override
    public AABB getRenderBoundingBox(T holder)
    {
        MobDisplay display = holder.getMobDisplay();
        double size = display != null ? display.size() : 1.0;
        return new AABB(holder.getBlockPos()).inflate(size / 2).expandTowards(0, holder.getMobDisplayHeight() + size, 0);
    }

    public static class State extends BlockEntityRenderState
    {
        // Reused every frame rather than making a new one
        final MobDisplayRenderer.Shown mob = new MobDisplayRenderer.Shown();
        double height;
    }
}
