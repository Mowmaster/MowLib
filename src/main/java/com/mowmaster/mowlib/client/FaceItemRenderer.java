package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.level.BlockAndTintGetter;

// Draws an item lying flat on one face of a block (an item frame's look: resolve it with ItemDisplayContext.FIXED), for
// block entity renderers. Call it from submit() with the pose stack at the block's corner (as renderers get it).
// Where on the face, in pixels from the face's centre, as seen by someone looking straight at that face:
// - right: towards their right hand; up: towards the top of their view
// - the top and bottom faces read as if looking at them from the south (their "up" is north)
// Items sit `lift` pixels out from the face so they never flicker against it.
public final class FaceItemRenderer
{
    private FaceItemRenderer() {}

    private static final float PX = 1f / 16f;
    // How thick the flattened item is drawn (it's a 3D model squashed against the face)
    private static final float DEPTH = 0.05f;

    // The light for things drawn on a face: the light of the space in front of it (inside a solid block it's dark)
    public static int light(BlockAndTintGetter level, BlockPos pos, Direction face)
    {
        return LightCoordsUtil.getLightCoords(level, pos.relative(face));
    }

    public static void submit(PoseStack poseStack, SubmitNodeCollector collector, ItemStackRenderState item, Direction face,
                              float right, float up, float size, float lift, int light)
    {
        if (item.isEmpty()) return;
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        // A FIXED item faces north (-z) as drawn: turn the north face to this one (the last rotate applies first)
        switch (face)
        {
            case SOUTH -> poseStack.rotate(Axis.YP.rotationDegrees(180));
            case EAST -> poseStack.rotate(Axis.YP.rotationDegrees(-90));
            case WEST -> poseStack.rotate(Axis.YP.rotationDegrees(90));
            case UP ->
            {
                // Tipped to face the sky, then half a turn so its top points north
                poseStack.rotate(Axis.XP.rotationDegrees(90));
                poseStack.rotate(Axis.ZP.rotationDegrees(180));
            }
            case DOWN -> poseStack.rotate(Axis.XP.rotationDegrees(-90));
            default -> {}
        }
        // On the north face, seen from the north: their right is -x, up is +y; the face is at z = -0.5
        poseStack.translate(-right * PX, up * PX, -0.5 - lift * PX);
        poseStack.scale(size * PX, size * PX, DEPTH);
        item.submit(poseStack, collector, light, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }
}
