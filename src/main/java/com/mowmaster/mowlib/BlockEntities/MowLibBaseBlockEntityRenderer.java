package com.mowmaster.mowlib.BlockEntities;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import org.joml.*;

import java.lang.Math;
import java.util.List;

public class MowLibBaseBlockEntityRenderer implements BlockEntityRenderer {
    public static Vector3f XN = new Vector3f(-1.0F, 0.0F, 0.0F);
    public static Vector3f XP = new Vector3f(1.0F, 0.0F, 0.0F);
    public static Vector3f YN = new Vector3f(0.0F, -1.0F, 0.0F);
    public static Vector3f YP = new Vector3f(0.0F, 1.0F, 0.0F);
    public static Vector3f ZN = new Vector3f(0.0F, 0.0F, -1.0F);
    public static Vector3f ZP = new Vector3f(0.0F, 0.0F, 1.0F);
    public static Vector3f ZERO = new Vector3f(0.0F, 0.0F, 0.0F);


    @Override
    public void render(BlockEntity p_112307_, float p_112308_, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_) {

    }

    public void renderItemRotating(Level worldIn, PoseStack p_112309_, MultiBufferSource p_112310_, ItemStack itemStack, int p_112311_, int p_112312_)
    {
        if (!itemStack.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(0.5, 1.0, 0.5);
            p_112309_.scale(0.75F, 0.75F, 0.75F);
            long time = System.currentTimeMillis();
            float angle = (time/25) % 360;
            p_112309_.mulPose(new Quaternionf(new AxisAngle4f(angle, YP)));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemStack,worldIn,null,0);
            renderer.render(itemStack, ItemDisplayContext.GROUND,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);

            //Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
        }
    }

    public void renderMultipleItemsRotating(Level level, PoseStack posStack, MultiBufferSource buffers, List<ItemStack> listed, int light, int overlay)
    {
        //https://github.com/VazkiiMods/Botania/blob/1.18.x/Xplat/src/main/java/vazkii/botania/client/render/tile/RenderTileRuneAltar.java#L49
        int stacks = listed.size();
        if (stacks>1) {
            posStack.pushPose();

            int items = stacks;
            float[] angles = new float[stacks];

            float anglePer = 360F / items;
            float totalAngle = 0F;
            for (int i = 0; i < angles.length; i++) {
                angles[i] = totalAngle += anglePer;
            }

            double time = level.getGameTime();


            float sized = 1.25F;
            float sizedd = 0.25F;
            if(stacks <= 4){sized = 0.25F; sizedd = 0.05F;}
            if(stacks <= 8 && stacks > 4){sized = 0.5F; sizedd = 0.10F;}
            if(stacks <= 12 && stacks > 8){sized = 0.75F; sizedd = 0.15F;}
            if(stacks <= 16 && stacks > 12){sized = 1.0F; sizedd = 0.20F;}

            for (int i = 0; i < stacks; i++) {
                posStack.pushPose();
                posStack.translate(0.5F, 0.75F, 0.5F);
                posStack.mulPose(new Quaternionf(new AxisAngle4f(angles[i] + (float) time, YP)));
                posStack.translate(sized, 0F, sizedd);
                posStack.mulPose(new Quaternionf(new AxisAngle4f(90F, YP)));
                posStack.translate(0D, 0.075 * Math.sin((time + i * 10) / 5D), 0F);
                ItemStack stack = listed.get(i);
                Minecraft mc = Minecraft.getInstance();
                if (!stack.isEmpty()) {
                    mc.getItemRenderer().renderStatic(stack, ItemDisplayContext.GROUND,
                            light, overlay, posStack, buffers, level,0);
                }
                posStack.popPose();
            }

            posStack.popPose();
        }
        else if(stacks==1)
        {
            posStack.pushPose();
            posStack.translate(0.5, 1.0, 0.5);
            posStack.scale(0.75F, 0.75F, 0.75F);
            long time = System.currentTimeMillis();
            float angle = (time/25) % 360;
            posStack.mulPose(new Quaternionf(new AxisAngle4f(angle, YP)));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(listed.get(0),level,null,0);
            renderer.render(listed.get(0), ItemDisplayContext.GROUND,true,posStack,buffers,light,overlay,baked);

            //Minecraft.getInstance().getItemRenderer().renderItem(itemStack, ItemCameraTransforms.TransformType.GROUND, p_112311_, p_112312_, posStack, p_112310_);
            posStack.popPose();
        }
    }

    public void renderStaticItem(Level worldIn,ItemStack itemTool, PoseStack p_112309_, MultiBufferSource p_112310_, int p_112311_, int p_112312_, float Xangle, float Yangle, float Zangle) {
        if (!itemTool.isEmpty()) {
            p_112309_.pushPose();
            p_112309_.translate(0.5D, 0.75D, 0.5D);
            p_112309_.scale(0.5F, 0.5F, 0.5F);

            p_112309_.mulPose(new Quaternionf(new AxisAngle4f(Xangle, XP)));
            p_112309_.mulPose(new Quaternionf(new AxisAngle4f(Yangle, YP)));
            p_112309_.mulPose(new Quaternionf(new AxisAngle4f(Zangle, ZP)));
            ItemRenderer renderer = Minecraft.getInstance().getItemRenderer();
            BakedModel baked = renderer.getModel(itemTool,worldIn,null,0);
            renderer.render(itemTool, ItemDisplayContext.FIXED,true,p_112309_,p_112310_,p_112311_,p_112312_,baked);
            //Minecraft.getInstance().getItemRenderer().renderItem(itemCoin, ItemCameraTransforms.TransformType.FIXED, p_112311_, p_112312_, p_112309_, p_112310_);
            p_112309_.popPose();
        }
    }

    //https://github.com/StanCEmpire/RegionProtection/blob/1.18.x/src/main/java/stancempire/stancempiresregionprotection/blockentities/RegionBlockBER.java
    public void renderBoundingBox(BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, MowLibBaseBlockEntity blockEntity, float red, float green, float blue, float alpha)
    {
        Matrix4f matrix4f = matrixStack.last().pose();
        Matrix3f matrix3f = matrixStack.last().normal();

        float minX = (float)(aabb.minX - pos.getX());
        float minY = (float)(aabb.minY - pos.getY());
        float minZ = (float)(aabb.minZ - pos.getZ());

        float maxX = (float)(aabb.maxX - pos.getX());
        float maxY = (float)(aabb.maxY - pos.getY());
        float maxZ = (float)(aabb.maxZ - pos.getZ());

        //Bottom
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, -1, 0).endVertex();

        //Top
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 1, 0).endVertex();

        //Sides
        buffer.vertex(matrix4f, minX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();

        buffer.vertex(matrix4f, maxX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();

        buffer.vertex(matrix4f, minX, minY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, 1).endVertex();

        buffer.vertex(matrix4f, maxX, minY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ).color(red, green, blue, alpha).normal(matrix3f, 0, 0, -1).endVertex();

    }

    public void renderFaces(TextureAtlasSprite sprite, BlockPos pos, AABB aabb, PoseStack matrixStack, VertexConsumer buffer, MowLibBaseBlockEntity blockEntity, float red, float green, float blue, float alpha)
    {
        Matrix4f matrix4f = matrixStack.last().pose();

        float minX = (float)(aabb.minX - pos.getX());
        float minY = (float)(aabb.minY - pos.getY());
        float minZ = (float)(aabb.minZ - pos.getZ());


        float maxX = (float)(aabb.maxX - pos.getX());
        float maxY = (float)(aabb.maxY - pos.getY());
        float maxZ = (float)(aabb.maxZ - pos.getZ());


        float minU = sprite.getU0();
        float maxU = sprite.getU1();
        float minV = sprite.getV0();
        float maxV = sprite.getV1();

        int uvBrightness = 240;

        //West
        buffer.vertex(matrix4f, minX - 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX - 0.01f, minY, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();

        buffer.vertex(matrix4f, minX + 0.01f, minY, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, minX + 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();


        //East
        buffer.vertex(matrix4f, maxX + 0.01f, minY, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX + 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX + 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX + 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(1, 0, 0).endVertex();

        buffer.vertex(matrix4f, maxX - 0.01f, minY, maxZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX - 0.01f, maxY, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX - 0.01f, maxY, minZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();
        buffer.vertex(matrix4f, maxX - 0.01f, minY, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(-1, 0, 0).endVertex();


        //North
        buffer.vertex(matrix4f, minX, maxY, minZ - 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ - 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ - 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, minY, minZ - 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();

        buffer.vertex(matrix4f, minX, minY, minZ + 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, minZ + 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, minZ + 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, minZ + 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();


        //South
        buffer.vertex(matrix4f, minX, minY, maxZ + 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ + 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ + 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();
        buffer.vertex(matrix4f, minX, maxY, maxZ + 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, 1).endVertex();

        buffer.vertex(matrix4f, minX, maxY, maxZ - 0.01f).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, maxY, maxZ - 0.01f).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, maxX, minY, maxZ - 0.01f).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();
        buffer.vertex(matrix4f, minX, minY, maxZ - 0.01f).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 0, -1).endVertex();


        //Bottom
        buffer.vertex(matrix4f, maxX, minY - 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY - 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY - 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY - 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();

        buffer.vertex(matrix4f, minX, minY + 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, minY + 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY + 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, minY + 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();


        //Top
        buffer.vertex(matrix4f, minX, maxY + 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY + 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY + 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY + 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, 1, 0).endVertex();

        buffer.vertex(matrix4f, maxX, maxY - 0.01f, minZ).color(red, green, blue, alpha).uv(minU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, maxX, maxY - 0.01f, maxZ).color(red, green, blue, alpha).uv(minU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY - 0.01f, maxZ).color(red, green, blue, alpha).uv(maxU, maxV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
        buffer.vertex(matrix4f, minX, maxY - 0.01f, minZ).color(red, green, blue, alpha).uv(maxU, minV).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(uvBrightness).normal(0, -1, 0).endVertex();
    }

    private void renderPedestalsHUD(PoseStack matrixStack, MultiBufferSource buffer, List<String> messages, float x, float y, float z, int angleX, int angleY, int angleZ) {


        matrixStack.pushPose();
        matrixStack.translate(x,y,z);
        //float f3 = 0.0075F;
        float f3 = 0.025F;
        matrixStack.scale(f3 , f3 , f3);

        matrixStack.mulPose(new Quaternionf(new AxisAngle4f(angleX, XP)));
        matrixStack.mulPose(new Quaternionf(new AxisAngle4f(angleY, YP)));
        matrixStack.mulPose(new Quaternionf(new AxisAngle4f(angleZ, ZP)));

        Font fontrenderer = Minecraft.getInstance().font;
        int lines = 11;
        int currenty = 7;
        int height = 10;
        int logsize = messages.size();
        int i = 0;
        for (String s : messages) {
            if (i >= logsize - lines) {
                if (currenty + height <= 124) {
                    String prefix = "";

                    //fontrenderer.plainSubstrByWidth(prefix + s, 115),7, currenty, 0xffffff, false, matrixStack.last().pose(), buffer, false, 0, 0xf000f0
                    MutableComponent comp = Component.empty();
                    fontrenderer.drawInBatch(fontrenderer.plainSubstrByWidth(prefix + s, 115),1.0f,1.0f,7,false,matrixStack.last().pose(), buffer, Font.DisplayMode.SEE_THROUGH,0, 0xf000f0);
                    currenty += height;
                }
            }
            i++;
        }


        matrixStack.popPose();
    }


}
