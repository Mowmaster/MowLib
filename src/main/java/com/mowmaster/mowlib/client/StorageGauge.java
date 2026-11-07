package com.mowmaster.mowlib.client;

import com.mowmaster.mowlib.transport.TransportStorage;
import com.mowmaster.mowlib.transport.TransportType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.network.chat.Component;
import net.neoforged.neoforge.transfer.fluid.FluidResource;
import net.neoforged.neoforge.transfer.resource.Resource;

import java.util.ArrayList;
import java.util.List;

// A gauge for one slot of any transport storage (a fluid tank, an energy or experience store...): a framed column
// filled from the bottom as far as the slot is full, with tick marks every eighth (longer at the quarters and half).
// Fluids show their own still texture and tint, other types their colour (TransportType#getRgb). tooltip() gives
// "Water: 1000 mB / 16000 mB" (TransportType#describeStored), or the type's name with 0 / capacity when empty.
// Generic over R so the storage's resource and the type agree (a wildcard TransportType<?> can call these through a
// small <R extends Resource> helper of its own).
public final class StorageGauge
{
    private StorageGauge() {}

    public static final int BORDER = PaperGui.SLOT_BORDER;
    public static final int FILL = PaperGui.SLOT_FILL;
    public static final int TICK = 0x993B2A1A;

    public static <R extends Resource> void draw(GuiGraphicsExtractor graphics, TransportType<R> type, TransportStorage<R> storage, int index,
                                                 int x, int y, int width, int height)
    {
        graphics.fill(x, y, x + width, y + height, BORDER);
        graphics.fill(x + 1, y + 1, x + width - 1, y + height - 1, FILL);
        if (index >= storage.size()) return;
        R resource = storage.getResource(index);
        long amount = storage.getAmountAsLong(index);
        long capacity = storage.getCapacityAsLong(index, resource);
        int inner = height - 2;
        int filled = capacity <= 0 || amount <= 0 ? 0 : (int) Math.max(1, Math.min(inner, amount * inner / capacity));
        if (filled > 0)
        {
            int top = y + 1 + inner - filled;
            if (resource instanceof FluidResource fluid) fluid(graphics, fluid, x + 1, top, width - 2, filled);
            else graphics.fill(x + 1, top, x + width - 1, y + 1 + inner, 0xFF000000 | type.getRgb());
        }
        for (int tick = 1; tick < 8; tick++)
        {
            int tickY = y + 1 + inner - inner * tick / 8;
            int length = tick == 4 ? 6 : tick % 2 == 0 ? 4 : 2;
            graphics.fill(x + 1, tickY, x + 1 + Math.min(length, width - 2), tickY + 1, TICK);
        }
    }

    public static <R extends Resource> List<Component> tooltip(TransportType<R> type, TransportStorage<R> storage, int index)
    {
        List<Component> lines = new ArrayList<>();
        if (index >= storage.size()) return lines;
        R resource = storage.getResource(index);
        long amount = storage.getAmountAsLong(index);
        long capacity = storage.getCapacityAsLong(index, resource);
        lines.add(amount > 0 ? type.describeStored(resource, amount, capacity)
                : Component.empty().append(type.getName()).append(": " + type.amountText(0) + " / " + type.amountText(capacity)));
        return lines;
    }

    // A fluid's still texture (its block model's sprite, the one the world uses), tinted the way NeoForge tints it in
    // buckets (MowLibClientParticles#fluidColor; -1 = untinted, like lava), stacked in squares up the column
    public static void fluid(GuiGraphicsExtractor graphics, FluidResource fluid, int x, int y, int width, int height)
    {
        TextureAtlasSprite sprite = Minecraft.getInstance().getModelManager().getFluidStateModelSet()
                .get(fluid.getFluid().defaultFluidState()).stillMaterial().sprite();
        int tint = MowLibClientParticles.fluidColor(fluid);
        int color = tint == -1 ? 0xFFFFFFFF : 0xFF000000 | tint;
        for (int drawn = 0; drawn < height; drawn += width)
        {
            int piece = Math.min(width, height - drawn);
            graphics.blitSprite(RenderPipelines.GUI_TEXTURED, sprite, x, y + height - drawn - piece, width, piece, color);
        }
    }
}
