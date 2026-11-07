package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.blockentity.ClientLoadedBlockEntities;
import com.mowmaster.mowlib.link.ILinkable;
import com.mowmaster.mowlib.link.LinkingToolItem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.rendertype.RenderTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;
import net.minecraft.util.context.ContextKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ExtractLevelRenderStateEvent;
import net.neoforged.neoforge.client.event.SubmitCustomGeometryEvent;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

// Link beams, shown while the player holds a LinkingToolItem (either hand): one along every link of the loaded
// linkables nearby (ILinkable tracked in ClientLoadedBlockEntities), in the sender's link colour, and one between the
// tool's saved block and the tool. The texture (textures/misc/link_beam.png) is a chevron that points towards the
// receiving end and slides along it. The tool's beam runs saved block -> hand in Send To mode and hand -> saved block
// in Receive From mode; out of the saved block's link range it stops at the edge of the range, turns red and stands
// still. Nothing to register: mods only implement ILinkable and track their block entities.
//
// 26.x draws the level in two steps, and NeoForge has an event for each: ExtractLevelRenderStateEvent copies the
// beams' ends into the frame's render state (the level can be read here), SubmitCustomGeometryEvent draws them.
@EventBusSubscriber(modid = MowLibReferences.MODID, value = Dist.CLIENT)
public final class LinkBeams
{
    private LinkBeams() {}

    public static final Identifier TEXTURE = MowLibReferences.id("textures/misc/link_beam.png");
    private static final ContextKey<Frame> BEAMS = new ContextKey<>(MowLibReferences.id("link_beams"));
    // Linkables further away than this (from the player) don't show their beams
    private static final double RANGE = 64;
    private static final float HALF_WIDTH = 0.07f;
    private static final float REPEATS_PER_BLOCK = 2f;
    // Texture lengths per tick
    private static final float SPEED = 0.05f;
    private static final int ALPHA = 0xC0;
    private static final int TOOL_BEAM_COLOR = 0xFFFFFF;
    private static final int OUT_OF_RANGE_COLOR = 0xFF3030;

    // One beam: from the sender's end to the receiver's, colour (0xAARRGGBB), whether its chevrons move
    private record Beam(Vec3 from, Vec3 to, int color, boolean animated) {}

    private record Frame(List<Beam> beams, float time) {}

    // ---- Step 1: what to draw ----

    @SubscribeEvent
    public static void extract(ExtractLevelRenderStateEvent event)
    {
        LocalPlayer player = Minecraft.getInstance().player;
        ClientLevel level = event.getLevel();
        if (player == null) return;
        InteractionHand toolHand = toolHand(player);
        if (toolHand == null) return;

        float partialTick = event.getDeltaTracker().getGameTimeDeltaPartialTick(false);
        List<Beam> beams = new ArrayList<>();
        for (ILinkable linkable : ClientLoadedBlockEntities.all(ILinkable.class, level))
        {
            if (linkable.getBlockPos().distToCenterSqr(player.position()) > RANGE * RANGE) continue;
            int color = (ALPHA << 24) | (linkable.getLinkColor() & 0xFFFFFF);
            Vec3 from = linkable.getLinkPoint();
            for (BlockPos link : linkable.getLinkList().get()) beams.add(new Beam(from, pointOf(level, link), color, true));
        }

        // The tool's saved block to the tool, at about where it is held
        ItemStack tool = player.getItemInHand(toolHand);
        BlockPos saved = LinkingToolItem.getSelected(tool);
        if (saved != null)
        {
            Vec3 hand = handPosition(player, toolHand, partialTick);
            Vec3 savedEnd = pointOf(level, saved);
            boolean inRange = true;
            if (level.getBlockEntity(saved) instanceof ILinkable savedLinkable)
            {
                inRange = savedLinkable.isInLinkRange(BlockPos.containing(hand));
                if (!inRange) hand = BeamRenderer.clipToRange(saved, savedLinkable.getLinkRange(), savedEnd, hand);
            }
            int color = (ALPHA << 24) | (inRange ? TOOL_BEAM_COLOR : OUT_OF_RANGE_COLOR);
            beams.add(LinkingToolItem.isReceiving(tool) ? new Beam(hand, savedEnd, color, inRange) : new Beam(savedEnd, hand, color, inRange));
        }
        if (beams.isEmpty()) return;
        event.getRenderState().setRenderData(BEAMS, new Frame(beams, level.getGameTime() + partialTick));
    }

    private static @Nullable InteractionHand toolHand(LocalPlayer player)
    {
        if (player.getMainHandItem().getItem() instanceof LinkingToolItem) return InteractionHand.MAIN_HAND;
        if (player.getOffhandItem().getItem() instanceof LinkingToolItem) return InteractionHand.OFF_HAND;
        return null;
    }

    // A linkable's own beam point, or the block's centre
    private static Vec3 pointOf(ClientLevel level, BlockPos pos)
    {
        return level.getBlockEntity(pos) instanceof ILinkable linkable ? linkable.getLinkPoint() : Vec3.atCenterOf(pos);
    }

    // Roughly where the tool is: in front of the eyes, a little down and out to the holding hand's side
    private static Vec3 handPosition(LocalPlayer player, InteractionHand hand, float partialTick)
    {
        Vec3 look = player.getViewVector(partialTick);
        Vec3 right = look.cross(new Vec3(0, 1, 0));
        right = right.lengthSqr() < 1.0E-4 ? new Vec3(1, 0, 0) : right.normalize();
        boolean rightHand = (hand == InteractionHand.MAIN_HAND) == (player.getMainArm() == HumanoidArm.RIGHT);
        return player.getEyePosition(partialTick).add(look.scale(0.6)).add(right.scale(rightHand ? 0.32 : -0.32)).add(0, -0.3, 0);
    }

    // ---- Step 2: hand the strips to the renderer ----

    @SubscribeEvent
    public static void submit(SubmitCustomGeometryEvent event)
    {
        Frame frame = event.getLevelRenderState().getRenderData(BEAMS);
        if (frame == null) return;
        Vec3 camera = event.getLevelRenderState().cameraRenderState.pos;
        PoseStack poseStack = event.getPoseStack();
        event.getSubmitNodeCollector().submitCustomGeometry(poseStack, RenderTypes.beaconBeam(TEXTURE, true), (pose, buffer) -> {
            for (Beam beam : frame.beams())
            {
                float scroll = beam.animated() ? frame.time() * SPEED : 0;
                BeamRenderer.strip(pose, buffer, beam.from().subtract(camera), beam.to().subtract(camera), HALF_WIDTH, beam.color(), REPEATS_PER_BLOCK, scroll);
            }
        });
    }
}
