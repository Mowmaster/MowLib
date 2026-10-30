package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import com.mowmaster.mowlib.display.ISpinningItemHolder;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

// Draws an ISpinningItemHolder's item spinning above the block (the block itself is a normal block model).
// Works for any mod's block entity type: event.registerBlockEntityRenderer(type, SpinningItemRenderer::new).
// <T extends BlockEntity & ISpinningItemHolder> means T must be a block entity AND implement the interface.
// 26.x renderers work in two steps: extractRenderState copies what's needed out of the block entity into a render
// state object, then submit draws from that state only, so the game can prepare one frame while drawing another.
// (Generalised from Bibliomania's BookRenderer.)
public class SpinningItemRenderer<T extends BlockEntity & ISpinningItemHolder> implements BlockEntityRenderer<T, SpinningItemRenderer.State>
{
    private final ItemModelResolver itemModelResolver;

    public SpinningItemRenderer(BlockEntityRendererProvider.Context context)
    {
        this.itemModelResolver = context.itemModelResolver();
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
        // GROUND: the same model and size as a dropped item. An empty stack gives an empty render state.
        itemModelResolver.updateForTopItem(state.item, holder.getDisplayedItem(), ItemDisplayContext.GROUND, holder.getLevel(), null, 0);
        state.height = holder.getDisplayHeight();
        state.scale = holder.getDisplayScale();
        long gameTime = holder.getLevel() != null ? holder.getLevel().getGameTime() : 0;
        // % 360 before adding the partial tick keeps the number small, so the float stays precise in long-running worlds
        state.angle = ((gameTime % 360) + partialTick) * holder.getSpinSpeed() % 360f;
    }

    @Override
    public void submit(State state, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        if (state.item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5, state.height, 0.5);
        poseStack.scale(state.scale, state.scale, state.scale);
        poseStack.rotate(Axis.YP.rotationDegrees(state.angle));
        state.item.submit(poseStack, submitNodeCollector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    public static class State extends BlockEntityRenderState
    {
        // Reused every frame rather than making a new one
        final ItemStackRenderState item = new ItemStackRenderState();
        double height;
        float scale;
        float angle;
    }
}
