package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.util.LightCoordsUtil;
import net.minecraft.world.phys.Vec3;

// Drawing textured beams in the world: flat strips turned to face the camera, full brightness, both sides. Write
// them into a SubmitCustomGeometryEvent's submitCustomGeometry callback with RenderTypes.beaconBeam(texture, true)
// (the beacon's own render type: see BeaconRenderer). v runs along the beam, u across it.
public final class BeamRenderer
{
    private BeamRenderer() {}

    // A strip from a to b (both relative to the camera), halfWidth wide each side, colour 0xAARRGGBB. The texture
    // repeats repeatsPerBlock times per block; scroll (in texture lengths) slides it from a towards b.
    public static void strip(PoseStack.Pose pose, VertexConsumer buffer, Vec3 a, Vec3 b, float halfWidth, int color, float repeatsPerBlock, float scroll)
    {
        Vec3 along = b.subtract(a);
        double length = along.length();
        if (length < 1.0E-3) return;
        // Sideways: at right angles to the beam and to the line from the camera (the origin) to the beam's middle
        Vec3 side = along.cross(a.add(b).scale(0.5));
        if (side.lengthSqr() < 1.0E-8) return;
        side = side.normalize().scale(halfWidth);

        float v0 = -scroll;
        float v1 = v0 + (float) length * repeatsPerBlock;
        Vec3 a0 = a.subtract(side), a1 = a.add(side), b1 = b.add(side), b0 = b.subtract(side);
        vertex(pose, buffer, a0, 0, v0, color);
        vertex(pose, buffer, a1, 1, v0, color);
        vertex(pose, buffer, b1, 1, v1, color);
        vertex(pose, buffer, b0, 0, v1, color);
        // The back, wound the other way, so it shows whichever side faces the camera
        vertex(pose, buffer, b0, 0, v1, color);
        vertex(pose, buffer, b1, 1, v1, color);
        vertex(pose, buffer, a1, 1, v0, color);
        vertex(pose, buffer, a0, 0, v0, color);
    }

    // The beacon beam's vertex format: position, colour, texture, overlay, light, normal
    private static void vertex(PoseStack.Pose pose, VertexConsumer buffer, Vec3 at, float u, float v, int color)
    {
        buffer.addVertex(pose, (float) at.x, (float) at.y, (float) at.z)
                .setColor(color)
                .setUv(u, v)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(LightCoordsUtil.FULL_BRIGHT)
                .setNormal(pose, 0, 1, 0);
    }

    // Where the line from start (inside the cube of blocks up to range from centre, on each axis) towards end leaves
    // that cube. Each axis says how far along the line its wall is (0 = start, 1 = end); the nearest wall wins.
    public static Vec3 clipToRange(BlockPos centre, int range, Vec3 start, Vec3 end)
    {
        Vec3 along = end.subtract(start);
        double[] origin = {start.x, start.y, start.z};
        double[] step = {along.x, along.y, along.z};
        double[] low = {centre.getX() - range, centre.getY() - range, centre.getZ() - range};
        double[] high = {centre.getX() + range + 1, centre.getY() + range + 1, centre.getZ() + range + 1};
        double t = 1;
        for (int axis = 0; axis < 3; axis++)
        {
            if (step[axis] > 0) t = Math.min(t, (high[axis] - origin[axis]) / step[axis]);
            else if (step[axis] < 0) t = Math.min(t, (low[axis] - origin[axis]) / step[axis]);
        }
        return start.add(along.scale(Math.max(0, t)));
    }
}
