package com.mowmaster.mowlib.client;

import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.fluid.FluidTintSource;
import net.neoforged.neoforge.transfer.fluid.FluidResource;

// Client-only particle helpers. Only call these from client-only code (or through a separate class that is only
// loaded on the client), so these client classes never load on a dedicated server.
public final class MowLibClientParticles
{
    private MowLibClientParticles()
    {
    }

    // The colour NeoForge tints the fluid with in buckets and tanks (0xRRGGBB), or -1 if it has no tint
    // (lava has its colour in the texture)
    public static int fluidColor(FluidResource resource)
    {
        FluidState state = resource.getFluid().defaultFluidState();
        FluidTintSource tint = Minecraft.getInstance().getModelManager().getFluidStateModelSet().get(state).fluidTintSource();
        if (tint == null) return -1;
        int color = tint.colorAsStack(resource.toStack(1000));
        return color == -1 ? -1 : color & 0xFFFFFF;
    }

    // Bubbles of the fluid moving from `from` to `to`: dust in the fluid's colour, or the fluid block's own
    // particles (which carry the texture) for fluids without a tint
    public static void fluid(Level level, Vec3 from, Vec3 to, FluidResource resource, RandomSource random)
    {
        FluidState state = resource.getFluid().defaultFluidState();
        int color = fluidColor(resource);
        Vec3 direction = to.subtract(from).normalize().scale(0.08);
        for (int bubble = 0; bubble < 10; bubble++)
        {
            Vec3 at = from.lerp(to, random.nextDouble()).add(random.nextGaussian() * 0.06, random.nextGaussian() * 0.06, random.nextGaussian() * 0.06);
            if (color != -1 && color != 0xFFFFFF)
            {
                level.addParticle(new DustParticleOptions(color, 0.6F + random.nextFloat() * 0.6F), at.x, at.y, at.z, direction.x, direction.y, direction.z);
            }
            else
            {
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, state.createLegacyBlock()), at.x, at.y, at.z, direction.x, direction.y, direction.z);
            }
        }
    }
}
