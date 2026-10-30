package com.mowmaster.mowlib.util;

import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.TrailParticleOption;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

// Particles that show something moving from one place to another. These call Level#addParticle, which only
// draws on the client: call them from client code (a payload handler, animateTick...). On a server use
// ServerLevel#sendParticles or your own payload instead.
public final class MowLibParticles
{
    private MowLibParticles()
    {
    }

    // Particles in colour rgb that each fly from near `from` to `to` in about a second (vanilla's trail particle)
    public static void addTrailParticles(Level level, Vec3 from, Vec3 to, int rgb, int count, RandomSource random)
    {
        for (int particle = 0; particle < count; particle++)
        {
            Vec3 start = from.add(random.nextGaussian() * 0.15, random.nextGaussian() * 0.15, random.nextGaussian() * 0.15);
            level.addParticle(new TrailParticleOption(to, rgb, 15 + random.nextInt(15)), start.x, start.y, start.z, 0, 0, 0);
        }
    }

    // count particles at random points along the line from `from` to `to`, each drifting on towards `to`
    public static void addParticlesAlong(Level level, ParticleOptions particle, Vec3 from, Vec3 to, int count, RandomSource random)
    {
        Vec3 direction = to.subtract(from).normalize().scale(0.1);
        for (int index = 0; index < count; index++)
        {
            Vec3 at = from.lerp(to, random.nextDouble()).add(random.nextGaussian() * 0.08, random.nextGaussian() * 0.08, random.nextGaussian() * 0.08);
            level.addParticle(particle, at.x, at.y, at.z, direction.x, direction.y + 0.05, direction.z);
        }
    }
}
