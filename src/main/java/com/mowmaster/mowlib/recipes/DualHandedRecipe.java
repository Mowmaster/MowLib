package com.mowmaster.mowlib.recipes;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.registries.codec.RegistryCodecs;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStackTemplate;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
//? if >=26.3 {
import net.neoforged.neoforge.registries.NewDatapackRegistryEvent;
//?} else
/*import net.neoforged.neoforge.registries.DataPackRegistryEvent;*/

// Dual Handed crafting: hold one thing in the offhand and another in the main hand, right-click a certain block, and get
// an item. Either input can be used up or kept, and the block can be destroyed. (1.20's dual-handed crafting, back as data.) DualHandedCrafting runs them.
//
// Data, like recipes, so any mod or datapack adds its own: one JSON file each in
//   data/<namespace>/mowlib/dual_handed_recipe/<name>.json
//   {
//     "offhand": "minecraft:paper",            // an ingredient: an item, "#a:tag", or a list
//     "mainhand": "minecraft:stick",
//     "block": "minecraft:bookshelf",          // a block, "#a:tag", or a list
//     "consume_offhand": true,                 // optional, default true: one is used up
//     "consume_mainhand": true,                // optional, default true
//     "consume_block": true,                   // optional, default false: the block is destroyed (no drops)
//     "result": { "id": "mowlib:tool_filter" } // an item, optionally with "count" and "components"
//   }
// In code (datagen), mods add them through GatherDataRegistryEntriesEvent: event.add(DualHandedRecipe.KEY, context ->
// context.register(ResourceKey.create(DualHandedRecipe.KEY, id), new DualHandedRecipe(...))).
//
// They're a NeoForge "datapack registry" (registered below): loaded from datapacks when a world starts, and sent to
// clients (so the client knows a click is a recipe and doesn't also use the block). Being a world registry, they're
// read once when the world loads: /reload doesn't pick up changed files. A record: an immutable holder whose
// constructor, getters, equals and toString Java writes for us.
@EventBusSubscriber(modid = MowLibReferences.MODID)
public record DualHandedRecipe(Ingredient offhand, Ingredient mainhand, HolderSet<Block> block,
                               boolean consumeOffhand, boolean consumeMainhand, boolean consumeBlock, ItemStackTemplate result)
{
    // The constructor from before consumeBlock, so code written against it keeps working (the block stays)
    public DualHandedRecipe(Ingredient offhand, Ingredient mainhand, HolderSet<Block> block,
                            boolean consumeOffhand, boolean consumeMainhand, ItemStackTemplate result)
    {
        this(offhand, mainhand, block, consumeOffhand, consumeMainhand, false, result);
    }

    // The registry's key: mowlib:dual_handed_recipe (so files go in data/<namespace>/mowlib/dual_handed_recipe/)
    public static final ResourceKey<Registry<DualHandedRecipe>> KEY = ResourceKey.createRegistryKey(MowLibReferences.id("dual_handed_recipe"));

    // RegistryCodecs.holderSet: one block, a #tag, or a list of blocks, as everywhere in vanilla data (26.3 moved it to
    // core.registries.codec and renamed it from homogeneousList)
    public static final Codec<DualHandedRecipe> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Ingredient.CODEC.fieldOf("offhand").forGetter(DualHandedRecipe::offhand),
                    Ingredient.CODEC.fieldOf("mainhand").forGetter(DualHandedRecipe::mainhand),
                    RegistryCodecs.holderSet(Registries.BLOCK).fieldOf("block").forGetter(DualHandedRecipe::block),
                    Codec.BOOL.optionalFieldOf("consume_offhand", true).forGetter(DualHandedRecipe::consumeOffhand),
                    Codec.BOOL.optionalFieldOf("consume_mainhand", true).forGetter(DualHandedRecipe::consumeMainhand),
                    Codec.BOOL.optionalFieldOf("consume_block", false).forGetter(DualHandedRecipe::consumeBlock),
                    ItemStackTemplate.CODEC.fieldOf("result").forGetter(DualHandedRecipe::result))
            .apply(instance, DualHandedRecipe::new));

    public boolean matches(Player player, BlockState state)
    {
        return offhand.test(player.getOffhandItem()) && mainhand.test(player.getMainHandItem()) && state.is(block);
    }

    // A world datapack registry, synced to clients with the same codec (so clients must have MowLib, which they do).
    // Stonecutter: 26.1 names the event DataPackRegistryEvent.NewRegistry and the method dataPackRegistry (the same
    // thing: loaded from datapacks with the world, the third argument syncs it to clients).
    //? if >=26.3 {
    @SubscribeEvent
    public static void registerRegistry(NewDatapackRegistryEvent event)
    {
        event.worldRegistry(KEY, CODEC, CODEC);
    }
    //?} else {
    /*@SubscribeEvent
    public static void registerRegistry(DataPackRegistryEvent.NewRegistry event)
    {
        event.dataPackRegistry(KEY, CODEC, CODEC);
    }
    *///?}
}
