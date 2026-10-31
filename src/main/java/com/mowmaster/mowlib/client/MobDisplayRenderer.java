package com.mowmaster.mowlib.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.logging.LogUtils;
import com.mojang.math.Axis;
import com.mowmaster.mowlib.display.MobDisplay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.sheep.Sheep;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

// Draws a MobDisplay (a mob spinning, facing the player or fixed; baby, adult or growing up; sheep coloured or
// sheared) for any block entity renderer. Works for any living mob whose renderer only needs the entity, which is
// nearly all of them; #mowlib:mob_display_blacklist lists the ones that don't (player, ender dragon...).
// It's how vanilla's mob spawner shows its spinning mob: a real entity is made on the client (never added to the
// world), set up, and turned into a render state by the EntityRenderDispatcher, which then draws that state.
//
// Use it in your renderer's two steps, like GhostItemRenderer (26.x renderers: extractRenderState copies, submit draws):
//   - keep one MobDisplayRenderer in your renderer, and a MobDisplayRenderer.Shown in your render state
//   - extractRenderState: mobRenderer.extract(state.mob, display, level, feet, cameraPos, partialTick, state.lightCoords)
//     where feet is the world position the mob stands on (e.g. Vec3.atLowerCornerOf(pos).add(0.5, 1, 0.5))
//   - submit: move the pose to the same spot (poseStack.translate(0.5, 1, 0.5) from the block's corner), then
//     mobRenderer.submit(state.mob, poseStack, submitNodeCollector, camera)
//   - getRenderBoundingBox: include the space above the block, or the mob disappears when the block is off screen
// For a block entity that only shows a mob, MobDisplayBlockRenderer does all of this.
public class MobDisplayRenderer
{
    private static final Logger LOGGER = LogUtils.getLogger();

    // Degrees per tick when spinning (2 = a turn every 9 seconds, like the spinning items)
    private static final float SPIN_SPEED = 2f;
    // The growing-up loop, in ticks: baby until BABY_UNTIL, grows until ADULT_FROM, adult until GROW_CYCLE
    private static final int GROW_CYCLE = 160;
    private static final int BABY_UNTIL = 60;
    private static final int ADULT_FROM = 90;

    // One entity per type, reused for every display of that type (each frame it's set up, then copied into a
    // render state straight away, so sharing it is safe). They belong to one level: a new level starts over.
    // WeakReference: doesn't keep an old level in memory after the player leaves it.
    private final Map<EntityType<?>, Entity> entities = new HashMap<>();
    private final Set<EntityType<?>> broken = new HashSet<>();
    private WeakReference<Level> entitiesLevel = new WeakReference<>(null);

    // ---- Step 1: extract (copy what's needed into the render state) ----

    public void extract(Shown shown, MobDisplay display, @Nullable Level level, Vec3 feet, Vec3 cameraPos, float partialTick, int lightCoords)
    {
        extract(shown, display, level, feet, cameraPos, partialTick, lightCoords, null);
    }

    // customize: optional, runs on the entity after MowLib's own set-up (age, sheep), e.g. to pick a cat or frog
    // variant. It's a client-side entity that never ticks and isn't in the world: only change how it looks.
    public void extract(Shown shown, MobDisplay display, @Nullable Level level, Vec3 feet, Vec3 cameraPos, float partialTick,
                        int lightCoords, @Nullable Consumer<Entity> customize)
    {
        shown.entity = null;
        if (level == null || !MobDisplay.canDisplay(display.type())) return;
        Entity entity = displayEntity(level, display.type());
        if (entity == null) return;

        // Standing where it's shown (some renderers look at the blocks around the entity)
        entity.snapTo(feet.x, feet.y, feet.z);
        // Game time kept small so the float stays precise; 24000 is a whole number of spins and growing loops
        float time = (level.getGameTime() % 24000) + partialTick;
        float growScale = applyAge(entity, display.age(), time, shown, level, feet);
        if (entity instanceof Sheep sheep)
        {
            sheep.setColor(display.sheepColor().orElse(DyeColor.WHITE));
            sheep.setSheared(display.sheared());
        }
        if (customize != null) customize.accept(entity);

        EntityRenderState renderState;
        try
        {
            renderState = dispatcher().extractEntity(entity, partialTick);
        }
        catch (RuntimeException e)
        {
            // A mob whose renderer needs more than a plain entity: never try it again this session
            LOGGER.warn("MowLib can't display {}, skipping it", display.type(), e);
            broken.add(display.type());
            return;
        }
        // Lit like the block it stands on, and no shadow (the shadow would be drawn full size at the entity's position)
        renderState.lightCoords = lightCoords;
        renderState.shadowRadius = 0f;
        renderState.shadowPieces.clear();

        shown.entity = renderState;
        shown.scale = fitScale(display) * growScale;
        shown.yaw = switch (display.facing())
        {
            case SPIN -> time * SPIN_SPEED % 360f;
            // atan2(x, z): the angle round from south (+z) towards east (+x), which is the way a model turns
            case FACE_PLAYER -> (float) Math.toDegrees(Math.atan2(cameraPos.x - feet.x, cameraPos.z - feet.z));
            case FIXED -> -display.yaw(); // entity yaw turns the other way round from a pose rotation
        };
    }

    // ---- Step 2: submit (draw from the render state only) ----

    // The pose must be at the mob's feet (not turned): this does the turning and scaling.
    public void submit(Shown shown, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, CameraRenderState camera)
    {
        if (shown.entity == null) return;
        poseStack.pushPose();
        poseStack.rotate(Axis.YP.rotationDegrees(shown.yaw));
        poseStack.scale(shown.scale, shown.scale, shown.scale);
        // Draws it at the pose's origin (0, 0, 0), as the spawner does
        dispatcher().submit(shown.entity, camera, 0.0, 0.0, 0.0, poseStack, submitNodeCollector);
        poseStack.popPose();
    }

    // ---- Set-up ----

    private static EntityRenderDispatcher dispatcher()
    {
        return Minecraft.getInstance().getEntityRenderDispatcher();
    }

    // The cached entity for this type, made the first time. Null if it can't be made or drawn.
    @Nullable
    private Entity displayEntity(Level level, EntityType<?> type)
    {
        if (entitiesLevel.get() != level)
        {
            entities.clear();
            entitiesLevel = new WeakReference<>(level);
        }
        if (broken.contains(type)) return null;
        // computeIfAbsent: looks it up, or makes it (and remembers it) if it isn't there yet
        Entity entity = entities.computeIfAbsent(type, key -> key.create(level, EntitySpawnReason.LOAD));
        // Players can't be made like this (create gives null), and non-living things (boats, items) aren't mobs
        if (!(entity instanceof LivingEntity))
        {
            entities.remove(type);
            broken.add(type);
            return null;
        }
        return entity;
    }

    // Shrinks mobs bigger than the display size (judged by the adult's size, so babies stay smaller)
    private static float fitScale(MobDisplay display)
    {
        EntityDimensions dimensions = display.type().getDimensions();
        float largest = Math.max(dimensions.width(), dimensions.height());
        return largest > display.size() ? display.size() / largest : 1f;
    }

    // Makes it a baby or an adult. Returns an extra scale for the growing animation (1 otherwise).
    // Mob#setBaby does nothing for mobs without babies (isBaby then stays false), so those just show as adults.
    private static float applyAge(Entity entity, MobDisplay.Age age, float time, Shown shown, Level level, Vec3 feet)
    {
        if (!(entity instanceof Mob mob)) return 1f;
        if (age != MobDisplay.Age.GROWING)
        {
            mob.setBaby(age == MobDisplay.Age.BABY);
            return 1f;
        }

        float inCycle = time % GROW_CYCLE;
        int cycle = (int) (time / GROW_CYCLE);
        if (inCycle >= ADULT_FROM)
        {
            mob.setBaby(false);
            // Once per loop, as it becomes an adult: a puff of happy-villager sparkles (like breeding)
            if (shown.grownCycle != cycle)
            {
                shown.grownCycle = cycle;
                puff(level, feet, mob.getBbWidth(), mob.getBbHeight());
            }
            return 1f;
        }

        mob.setBaby(true);
        if (inCycle < BABY_UNTIL || !mob.isBaby()) return 1f;
        // Growing: the baby swells smoothly until it's adult height, then turns into the adult
        float progress = (inCycle - BABY_UNTIL) / (ADULT_FROM - BABY_UNTIL);
        float smooth = progress * progress * (3 - 2 * progress); // smoothstep: starts and ends gently
        float adultHeight = mob.getType().getDimensions().height();
        float babyHeight = Math.max(0.01f, mob.getBbHeight());
        return 1f + (adultHeight / babyHeight - 1f) * smooth;
    }

    // Client-side only particles (Level#addParticle draws on the client; extract runs on the client)
    private static void puff(Level level, Vec3 feet, float width, float height)
    {
        RandomSource random = level.getRandom();
        for (int i = 0; i < 8; i++)
        {
            level.addParticle(ParticleTypes.HAPPY_VILLAGER,
                    feet.x + (random.nextDouble() - 0.5) * width,
                    feet.y + random.nextDouble() * height,
                    feet.z + (random.nextDouble() - 0.5) * width,
                    0, 0, 0);
        }
    }

    // The mob part of a render state. Kept between frames.
    public static class Shown
    {
        @Nullable EntityRenderState entity;
        float yaw;
        float scale = 1f;
        // The growing loop that last puffed, so it puffs once per loop rather than every frame
        int grownCycle = -1;

        public boolean isEmpty()
        {
            return entity == null;
        }
    }
}
