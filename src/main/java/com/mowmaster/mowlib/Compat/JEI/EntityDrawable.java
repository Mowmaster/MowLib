package com.mowmaster.mowlib.Compat.JEI;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mowmaster.mowlib.Client.EntityWidget;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.phys.Vec3;

public class EntityDrawable implements IDrawableAnimated
{

    private LivingEntity livingEntity;
    private int width;
    private int height;

    public EntityDrawable(int height, int width, Item resourceItem, LivingEntity livingEntity) {
        this.livingEntity = livingEntity;
        this.width = width;
        this.height = height;
    }

    @Override
    public int getWidth() {
        return this.width;
    }

    @Override
    public int getHeight() {
        return this.height;
    }

    @Override
    public void draw(PoseStack poseStack, int xOffset, int yOffset) {
        EntityWidget.renderEntity(poseStack, livingEntity, new Vec3(15, -225, 0),
                new Vec3(32, 32, 32), Vec3.ZERO, xOffset, yOffset);
    }
}
