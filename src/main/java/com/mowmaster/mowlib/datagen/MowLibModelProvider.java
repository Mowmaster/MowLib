package com.mowmaster.mowlib.datagen;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.registry.MowLibItems;
import net.minecraft.client.color.item.Dye;
import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.model.ItemModelUtils;
import net.minecraft.client.data.models.model.ModelLocationUtils;
import net.minecraft.client.data.models.model.ModelTemplates;
import net.minecraft.client.data.models.model.TextureMapping;
import net.minecraft.client.resources.model.sprite.Material;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

import java.util.stream.Stream;

// Item models for the colour picker and custom dye. The filters, tools and work cards have hand-written models in
// src/main/resources/assets/mowlib/items and models/item (one per filter mode), so they're left out here.
public class MowLibModelProvider extends ModelProvider
{
    public MowLibModelProvider(PackOutput output)
    {
        super(output, MowLibReferences.MODID);
    }

    // NeoForge's ModelProvider checks that every block and item in the mowlib namespace got a model here, and fails
    // ("Missing item model definitions for: ...") if not. MowLib has no blocks, and only these two items are made
    // here, so the check is limited to them. Add an item to this list when you add its model below.
    // Stream.of(...) makes a stream of the given values; Holder is the registry entry an item or block lives in.
    @Override
    protected Stream<? extends Holder<Block>> getKnownBlocks()
    {
        return Stream.empty();
    }

    @Override
    protected Stream<? extends Holder<Item>> getKnownItems()
    {
        return Stream.of(MowLibItems.COLOR_PICKER, MowLibItems.CUSTOM_DYE);
    }

    @Override
    protected void registerModels(BlockModelGenerators blockModels, ItemModelGenerators itemModels)
    {
        // Colour picker: color_picker.png when empty, color_picker_dyed.png once it holds a colour
        Item picker = MowLibItems.COLOR_PICKER.get();
        Identifier emptyPicker = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(picker),
                TextureMapping.layer0(new Material(MowLibReferences.id("item/color_picker"))),
                blockModels.modelOutput);
        Identifier dyedPicker = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(picker, "_dyed"),
                TextureMapping.layer0(new Material(MowLibReferences.id("item/color_picker_dyed"))),
                blockModels.modelOutput);
        blockModels.itemModelOutput.accept(picker, ItemModelUtils.conditional(
                ItemModelUtils.hasComponent(DataComponents.DYED_COLOR),
                ItemModelUtils.plainModel(dyedPicker),     // has a picked colour
                ItemModelUtils.plainModel(emptyPicker)));  // empty

        // Custom dye: vanilla's white dye texture, tinted with the dye's stored colour
        Identifier customDyeModel = ModelTemplates.FLAT_ITEM.create(
                ModelLocationUtils.getModelLocation(MowLibItems.CUSTOM_DYE.get()),
                TextureMapping.layer0(new Material(Identifier.withDefaultNamespace("item/white_dye"))),
                blockModels.modelOutput);
        blockModels.itemModelOutput.accept(MowLibItems.CUSTOM_DYE.get(),
                ItemModelUtils.tintedModel(customDyeModel, new Dye(0xFFFFFFFF)));
    }
}
