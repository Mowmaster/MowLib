package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.ghost.GhostItem;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.item.ItemStackRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

// Draws GhostItems (missing inputs) for any block entity renderer: pulsing items that face the camera, with
// labels above and below, floating over the block they belong on or circling the machine itself.
// Use it in your renderer's two steps (26.x renderers: extractRenderState copies, submit draws):
//   - keep a GhostItemRenderer (made from the renderer context's item model resolver and font) and a
//     GhostItemRenderer.Ghosts in your render state
//   - extractRenderState: ghostRenderer.extract(state.ghosts, blockEntity.getGhosts(), level, pos, seed)
//   - submit (pose at the block's corner, not turned): ghostRenderer.submit(state.ghosts, facing, spin, poseStack, collector, camera)
//   - getRenderBoundingBox: GhostItemRenderer.boundingBox(pos, ghosts)
public class GhostItemRenderer
{
    // Block and sky light both at full: ghosts glow the same in the dark
    private static final int FULL_BRIGHT = 0xF000F0;
    // Labels: half a name tag's size (name tags use 0.025 blocks per font pixel), white on vanilla's dark backing.
    // Heights are from the ghost's centre to the top edge of the text.
    private static final float TEXT_SCALE = 0.0125f;
    private static final float LABEL_ABOVE = 0.6f;
    private static final float LABEL_BELOW = -0.3f;
    private static final int TEXT_COLOR = 0xFFFFFFFF;
    private static final int TEXT_BACKGROUND = 0x40000000;

    private final ItemModelResolver itemModelResolver;
    private final Font font;

    public GhostItemRenderer(ItemModelResolver itemModelResolver, Font font)
    {
        this.itemModelResolver = itemModelResolver;
        this.font = font;
    }

    // Copies the ghosts into the render state. The render states are kept between frames and only added to, so
    // nothing is made every frame. Ghosts with an "above" position float just past that block's face: its facing
    // property's direction if it has one (like a pedestal), otherwise its top.
    public void extract(Ghosts state, List<GhostItem> ghosts, @Nullable Level level, BlockPos origin, int seed)
    {
        while (state.ghosts.size() < ghosts.size()) state.ghosts.add(new Ghost());
        state.count = ghosts.size();
        state.orbitCount = 0;
        for (int index = 0; index < ghosts.size(); index++)
        {
            GhostItem ghost = ghosts.get(index);
            Ghost render = state.ghosts.get(index);
            itemModelResolver.updateForTopItem(render.item, ghost.stack(), ItemDisplayContext.GROUND, level, null, seed + index);
            render.label = ghost.label();
            render.below = ghost.below().orElse(null);
            if (ghost.above().isPresent() && level != null)
            {
                BlockPos at = ghost.above().get();
                Direction facing = level.getBlockState(at).getOptionalValue(BlockStateProperties.FACING).orElse(Direction.UP);
                render.offset = Vec3.atCenterOf(at).subtract(Vec3.atLowerCornerOf(origin)).add(facing.getUnitVec3().scale(0.95));
                render.orbitIndex = -1;
            }
            else render.orbitIndex = state.orbitCount++;
        }
    }

    // Draws them. The pose must be at the block's lower corner (as renderers get it), not turned. facing is the
    // way the machine points (UP for an ordinary block): circling ghosts go round that axis. spin is in degrees
    // and drives the circling, bobbing and pulsing (e.g. 2 degrees a tick).
    public void submit(Ghosts state, Direction facing, float spin, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        for (int index = 0; index < state.count; index++)
        {
            Ghost ghost = state.ghosts.get(index);
            Vec3 offset = ghost.orbitIndex < 0 ? ghost.offset : orbitOffset(state, facing, spin, ghost.orbitIndex);
            poseStack.pushPose();
            poseStack.translate(offset.x, offset.y, offset.z);
            submitGhost(ghost, spin, poseStack, submitNodeCollector, camera);
            poseStack.popPose();
        }
    }

    // Where a circling ghost is now: evenly spaced round the machine, or straight above it if it's the only one.
    // Worked out as if the machine stood on the ground, then turned the way it faces (Vector3f.rotate applies the
    // quaternion to the point).
    private static Vec3 orbitOffset(Ghosts state, Direction facing, float spin, int orbitIndex)
    {
        Vector3f point;
        if (state.orbitCount == 1) point = new Vector3f(0f, 1.1f, 0f);
        else
        {
            double angle = Math.toRadians(spin + 360.0 * orbitIndex / state.orbitCount);
            point = new Vector3f((float) Math.cos(angle) * 0.9f, 0.7f, (float) Math.sin(angle) * 0.9f);
        }
        point.rotate(facing.getRotation());
        return new Vec3(0.5 + point.x(), 0.5 + point.y(), 0.5 + point.z());
    }

    // A ghost: bobbing and pulsing, lit at full brightness, always turned to face the camera (like a thrown snowball),
    // with its labels above and below
    private void submitGhost(Ghost ghost, float spin, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        double wave = Math.toRadians(spin * 2);
        poseStack.pushPose();
        poseStack.translate(0f, (float) (0.05 * Math.sin(wave)), 0f);
        if (ghost.label != null) submitLabel(ghost.label, LABEL_ABOVE, poseStack, submitNodeCollector, camera);
        if (ghost.below != null) submitLabel(ghost.below, LABEL_BELOW, poseStack, submitNodeCollector, camera);
        poseStack.rotate(camera.orientation);
        float size = 0.675f + 0.075f * (float) Math.sin(wave * 2);
        poseStack.scale(size, size, size);
        ghost.item.submit(poseStack, submitNodeCollector, FULL_BRIGHT, OverlayTexture.NO_OVERLAY, 0);
        poseStack.popPose();
    }

    // One line of text facing the camera, centred, its top edge y above the current point. This is what a name tag
    // does (SubmitNodeCollection#submitNameTag), at our own size and height. The scale's y is negative because text
    // is laid out downwards (screen style) while the world's y goes up.
    public void submitLabel(Component text, float y, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        poseStack.pushPose();
        poseStack.translate(0f, y, 0f);
        poseStack.rotate(camera.orientation);
        poseStack.scale(TEXT_SCALE, -TEXT_SCALE, TEXT_SCALE);
        FormattedCharSequence line = text.getVisualOrderText();
        submitNodeCollector.submitText(poseStack, -font.width(line) / 2f, 0f, line, false, Font.DisplayMode.NORMAL, FULL_BRIGHT, TEXT_COLOR, TEXT_BACKGROUND, 0);
        poseStack.popPose();
    }

    // The block and the space around it, out to every block a ghost floats over (for getRenderBoundingBox: NeoForge
    // skips renderers whose box is off screen)
    public static AABB boundingBox(BlockPos origin, List<GhostItem> ghosts)
    {
        AABB box = new AABB(origin).inflate(1.5);
        for (GhostItem ghost : ghosts)
        {
            if (ghost.above().isPresent()) box = box.minmax(new AABB(ghost.above().get()).inflate(1.5));
        }
        return box;
    }

    // The ghosts part of a render state
    public static class Ghosts
    {
        final List<Ghost> ghosts = new ArrayList<>();
        int count;
        int orbitCount;

        public boolean isEmpty()
        {
            return count == 0;
        }
    }

    // One ghost: orbitIndex is its place in the circle round the machine, or -1 when it floats at offset
    static class Ghost
    {
        final ItemStackRenderState item = new ItemStackRenderState();
        @Nullable Component label;
        @Nullable Component below;
        Vec3 offset = Vec3.ZERO;
        int orbitIndex = -1;
    }
}
