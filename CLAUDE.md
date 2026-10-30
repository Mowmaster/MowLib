NeoForge 26.3.0.94, Minecraft 26.3. I'm coming from 1.20 and JDK 8. Explain new APIs and modern Java features as you use them. I copy code in myself, so give complete, clearly placed snippets.

# MowLib

A library mod (mod ID `mowlib`, package `com.mowmaster.mowlib`) holding code shared by my mods. Mods depend on it
and bundle it with Jar-in-Jar, so players don't install it separately.

## Rules for this project
- Mechanisms, not content: shared systems, base classes and utilities live here. Actual blocks (beams, chests,
  piles, windows...) stay in the mods.
- Three mods depend on this. Don't change public signatures casually; when something has to change, say so and list
  what each mod must update. Bump `mod_version` (semver: breaking change = major).
- Anything shared that needs registering (block entity types, the picker and dye items, the recipe serializer, the
  data map) is registered here under `mowlib:`. Shared block entity types find their valid blocks by scanning
  `BuiltInRegistries.BLOCK` (every mod's blocks are registered before block entity types), so mods don't list them.
- Mods that move something here keep old worlds loading with registry aliases
  (`DeferredRegister#addAlias(oldModId:name, mowlib:name)` in the mod), see `MIGRATION_CRYSTALBLOCKS.md`.
- New code arrives as zips from my other projects' sessions (see `MOWLIB_EXTRACTION_PROMPT.txt`). Each zip has a
  `NOTES.md`. When merging, compare with what's already here and combine features rather than adding parallel copies.

## What's here
- `tint/` - colourable blocks
  - `IColorableBlock`: implement on a block to make it dyeable. Tint regions (`getTintRegionCount`,
    `getTintRegion` from the clicked point, `getRegionForTintIndex`, `getAddedRegion` for slabs becoming doubles),
    `getDefaultColor(region)`, `tryDyeBlock` (leather-armour colour mixing; dyes are not used up), `dyeColorOf`
    (vanilla dyes + Custom Dye). Extends `EntityBlock`: by default the block gets a `CBTintBlockEntity`.
  - `ITintHolder`: what dyeing, the picker and the tint sources read colours from.
  - `CBTintBlockEntity`: plain block entity holding one colour per region (`mowlib:tint_block_entity`). Saves as
    "color", "color1", ...; syncs to clients and re-meshes the chunk.
  - `CBTintData`: the same colour storage, for block entities that must extend a vanilla class (chests, barrels).
    Such blocks return false from `usesTintBlockEntity()` and create their own block entity.
- `client/CBBlockEntityTintSource` + `MowLibClient`: one tint source per tint region for every `IColorableBlock`
  in every mod, registered automatically. A model face's `tintindex` = region. Mods must not register their own.
- `util/CBPixelBox`: a box in pixels (0-16) like a model element; rotate/offset/union, shapes, click-to-pixel.
- `storage/`
  - `CBBulkItemHandler`: extends NeoForge's `ResourceStacksResourceHandler<ItemResource>` (transfer API). Each slot
    holds one item type (item + data components) and an int amount up to `capacity`, never an `ItemStack` (those cap
    at 99 when saved). Empties reset the slot. `insertStack`/`extractStack` player helpers run their own
    `Transaction`. Saved via `serialize`/`deserialize` (key "stacks").
  - `CBBulkStorageBlock` (+ `CBBulkStorageBlockEntity`, shared type `mowlib:bulk_storage`): right-click inserts the
    held stack, left-click takes 1, sneak-left-click 64; creative players extract instead of breaking until empty;
    breaking spills the contents; decorated pot sounds and dust; comparator output; tintable. Subclass with a shape,
    tint regions, capacity and an item tag: `super(properties, 3456, Tags.Items.INGOTS)`.
  - `MowLibCapabilities`: the bulk storage item capability, for hoppers and pipes.
- `items/CBColorPickerItem` (`mowlib:color_picker`): right-click a block to store its colour (from `ITintHolder`, or
  the `mowlib:block_colors` data map for other blocks). Crafted alone it gives a `mowlib:custom_dye` of that colour
  (`recipes/CBPickedDyeRecipe`, serializer `mowlib:picked_dye`).
- `datamaps/`: `CBBlockColor` (`{"color": "#RRGGBB"}`) and `CBDataMaps.BLOCK_COLORS`. `datagen/CBBlockColorProvider`
  fills it with the average texture colour of plain solid cubes (vanilla, plus anything loaded during datagen).

## 26.x notes (learned in CrystalBlocks)
- Render layer comes from texture alpha; use `noOcclusion()` on see-through blocks.
- Datagen: models via `ModelProvider`/`BlockModelGenerators`; recipes and loot via `GatherDataRegistryEntriesEvent`.
- Block entity save/load uses `ValueOutput`/`ValueInput`; `updateShape` gets a `ScheduledTickAccess`;
  `useItemOn` returns `InteractionResult.SUCCESS / PASS / TRY_WITH_EMPTY_HAND`.
- Item transfer API: `ResourceHandler<ItemResource>`, `Transaction.openRoot()`, `VanillaContainerWrapper`.

## Model conventions
- Texture slots `#texture_one/two/three`; `cullface` only on faces at the block edge; glowing elements use
  `"light_emission": 15` + `"neoforge_data": {"ambient_occlusion": false}`.

## Working with Claude
- Pull and read the repo only; never commit or push. I handle git.
- For simple model fixes (a missing `tintindex`, `#`, texture key), just tell me the line to change; only send a model file when the geometry changes.
- To check 26.3 APIs, read the public `neoforged/neoforge` repo (branch `26.3.x`) and the porting primers in `neoforged/.github` (`primers/26.1`, `26.2`, `26.3`). NeoForge's docs are in `neoforged/Documentation`.
- Reference implementation of everything here: `Mowmaster/CrystalBlocks`, branch `NeoForge-26x`.

## Project State
- transport: TransportType registry (mowlib:transport_type; items/fluids/energy/experience). A type = how to
  store, reach (block/entity/item handlers), save, filter, drop, absorb and show one kind of thing; machine
  numbers (rates, sizes) belong to the machine (host.getCapacity). TransportContents = contents in an item.
  mowlib:experience_handler block + item capabilities.
- filter: ITransportFilter items (12, mowlib:filter_*), FilterData, IFilterHolder (block entities with a
  filter slot), filter tool. workcard: IWorkCard/IWorkCardHolder, area + locations cards, work card tool.
- ghost: GhostItem + client.GhostItemRenderer (missing-input displays). storage.StorageInteractions: the
  shared right-click insert / left-click take / filter slot clicks.
- util: MowLibXpUtils, MowLibOwner + MowLibFakePlayer, MowLibBlockActions, MowLibTextUtils, MowLibParticles;
  client: MowLibClientParticles. CBPixelBox.rotateFromUp for six-facing models.