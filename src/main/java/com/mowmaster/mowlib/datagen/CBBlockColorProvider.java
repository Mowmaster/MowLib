package com.mowmaster.mowlib.datagen;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.blaze3d.platform.NativeImage;
import com.mowmaster.mowlib.tint.IColorableBlock;
import com.mowmaster.mowlib.datamaps.CBBlockColor;
import com.mowmaster.mowlib.datamaps.CBDataMaps;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.common.data.DataMapProvider;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

// Fills the block_colors data map with the average colour of every solid, untinted, full-cube block (vanilla and
// any other mod loaded during datagen). Mods that depend on MowLib can run their own copy for their blocks.
public class CBBlockColorProvider extends DataMapProvider
{
    private final ResourceManager clientResources;

    public CBBlockColorProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookupProvider, ResourceManager clientResources)
    {
        super(output, lookupProvider);
        this.clientResources = clientResources;
    }

    @Override
    protected void gather(HolderLookup.Provider provider)
    {
        var colors = builder(CBDataMaps.BLOCK_COLORS);
        for (Block block : BuiltInRegistries.BLOCK)
        {
            Identifier id = BuiltInRegistries.BLOCK.getKey(block);
            // Colourable blocks report their own colour to the picker; only plain solid cubes get an entry
            if (block instanceof IColorableBlock || !isSolidFullCube(block)) continue;

            averageColor(id).ifPresent(rgb -> colors.add(
                    BuiltInRegistries.BLOCK.getResourceKey(block).orElseThrow(), new CBBlockColor(rgb), false));
        }
    }

    private static boolean isSolidFullCube(Block block)
    {
        try
        {
            BlockState state = block.defaultBlockState();
            return state.canOcclude() && Block.isShapeFullBlock(state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO));
        }
        catch (RuntimeException e)
        {
            return false; // some blocks need a real world to work out their shape; skip them
        }
    }

    // Blockstate -> model (and its parents) -> particle texture -> average colour. Empty if any step fails or the model is tinted.
    private Optional<Integer> averageColor(Identifier blockId)
    {
        JsonObject blockState = readJson(Identifier.fromNamespaceAndPath(blockId.getNamespace(), "blockstates/" + blockId.getPath() + ".json"));
        if (blockState == null || !blockState.has("variants")) return Optional.empty();

        JsonObject variants = blockState.getAsJsonObject("variants");
        if (variants.isEmpty()) return Optional.empty();
        JsonElement first = variants.entrySet().iterator().next().getValue();
        JsonObject variant = first.isJsonArray() ? first.getAsJsonArray().get(0).getAsJsonObject() : first.getAsJsonObject();

        // Walk up the model's parents: the nearest model's textures win, and the first elements found are the ones used
        Map<String, String> textures = new HashMap<>();
        JsonArray elements = null;
        Identifier modelId = Identifier.parse(variant.get("model").getAsString());
        for (int depth = 0; modelId != null && depth < 16; depth++)
        {
            JsonObject model = readJson(Identifier.fromNamespaceAndPath(modelId.getNamespace(), "models/" + modelId.getPath() + ".json"));
            if (model == null) break;

            if (model.has("textures"))
            {
                for (var entry : model.getAsJsonObject("textures").entrySet())
                {
                    textures.putIfAbsent(entry.getKey(), textureName(entry.getValue()));
                }
            }
            if (elements == null && model.has("elements")) elements = model.getAsJsonArray("elements");
            modelId = model.has("parent") ? Identifier.parse(model.get("parent").getAsString()) : null;
        }

        if (elements == null || hasTint(elements)) return Optional.empty();
        String texture = resolveTexture(textures, "particle");
        return texture == null ? Optional.empty() : averageTexture(Identifier.parse(texture));
    }

    // A texture value is either "namespace:path" or, in 26.x, an object with a "sprite"
    private static String textureName(JsonElement value)
    {
        return value.isJsonObject() ? value.getAsJsonObject().get("sprite").getAsString() : value.getAsString();
    }

    // Follows "#other" references until a real texture name is found
    @Nullable
    private static String resolveTexture(Map<String, String> textures, String key)
    {
        String value = textures.get(key);
        for (int i = 0; value != null && value.startsWith("#") && i < 16; i++)
        {
            value = textures.get(value.substring(1));
        }
        return value != null && !value.startsWith("#") ? value : null;
    }

    // Vanilla only tints faces that have a tintindex
    private static boolean hasTint(JsonArray elements)
    {
        for (JsonElement element : elements)
        {
            JsonObject faces = element.getAsJsonObject().getAsJsonObject("faces");
            if (faces == null) continue;
            for (var face : faces.entrySet())
            {
                if (face.getValue().getAsJsonObject().has("tintindex")) return true;
            }
        }
        return false;
    }

    // Average of all visible pixels (animated textures average every frame)
    private Optional<Integer> averageTexture(Identifier texture)
    {
        Optional<Resource> resource = clientResources.getResource(
                Identifier.fromNamespaceAndPath(texture.getNamespace(), "textures/" + texture.getPath() + ".png"));
        if (resource.isEmpty()) return Optional.empty();

        try (InputStream input = resource.get().open(); NativeImage image = NativeImage.read(input))
        {
            long red = 0, green = 0, blue = 0, count = 0;
            for (int y = 0; y < image.getHeight(); y++)
            {
                for (int x = 0; x < image.getWidth(); x++)
                {
                    int argb = image.getPixel(x, y);
                    if ((argb >>> 24) == 0) continue; // fully transparent
                    red += (argb >> 16) & 0xFF;
                    green += (argb >> 8) & 0xFF;
                    blue += argb & 0xFF;
                    count++;
                }
            }
            if (count == 0) return Optional.empty();
            return Optional.of((int) (red / count) << 16 | (int) (green / count) << 8 | (int) (blue / count));
        }
        catch (IOException | RuntimeException e)
        {
            return Optional.empty();
        }
    }

    @Nullable
    private JsonObject readJson(Identifier location)
    {
        Optional<Resource> resource = clientResources.getResource(location);
        if (resource.isEmpty()) return null;
        try (Reader reader = resource.get().openAsReader())
        {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
        catch (IOException | RuntimeException e)
        {
            return null;
        }
    }
}