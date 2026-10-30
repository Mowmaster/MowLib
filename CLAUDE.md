NeoForge 26.3.0.94, Minecraft 26.3, Java 25. I'm coming from 1.20 and JDK 8. Explain new APIs and modern Java features as you use them. I copy code in myself, so give complete, clearly placed snippets.

# MowLib

A library mod (mod ID `mowlib`, package `com.mowmaster.mowlib`) holding code shared by my mods. Mods depend on it
and bundle it with Jar-in-Jar, so players don't install it separately.

## Rules for this project
- Mechanisms, not content: shared systems, base classes and utilities live here. Actual blocks (beams, chests,
  piles, windows...) stay in the mods. MowLib registers no blocks, not even examples.
- Mods depend on this. Don't change public signatures casually; when something has to change, say so and list what
  each mod must update (see Releases).
- Anything shared that needs registering is registered here under `mowlib:`, from `MowLib`'s constructor
  (DeferredRegisters) or an `@EventBusSubscriber` class. Shared block entity types find their valid blocks by scanning
  `BuiltInRegistries.BLOCK` (every mod's blocks are registered before block entity types), so mods don't list them.
  They're built with the `Set<Block>` constructor, which allows an empty set (the varargs one throws when empty).
- Mods that move something here keep old worlds loading with registry aliases
  (`DeferredRegister#addAlias(oldModId:name, mowlib:name)` in the mod), listed in that mod's `MIGRATION_<modid>.md`.
- Strings: all English text is in `datagen/MowLibLanguageProvider`. There is no hand-written `lang/en_us.json`.

## New code from other projects
- New MowLib code arrives as `mowlib-from-<modid>.zip` extractions made in a mod's session, following
  `MOWLIB_EXTRACTION_PROMPT.txt`. Layout: `mowlib/` (files at MowLib repo paths), `notes/`, `<modid>-migration/`
  (the mod's changed files), `NOTES.md`, `MIGRATION_<modid>.md`.
- A MowLib session merging a zip compares every file with what's already here and combines features into the existing
  class. Never add a parallel copy (a second tint block entity, another item handler...). If two versions really
  differ, keep one class, add the missing behaviour to it, and deprecate the other's methods.
- Merging into MowLib is a minor release at most if it only adds; anything that changes existing behaviour or
  signatures follows the Releases rules.

## What's here
- Root: `MowLib` (registers everything), `MowLibClient` (client registrations), `MowLibReferences` (`MODID`,
  `id(path)`), `MowLibCapabilities` (bulk storage item capability).
- `registry/` - `MowLibItems`, `MowLibBlockEntities`, `MowLibDataComponents`, `MowLibRecipes`.
  `creative/CreativeTabRegistry`: the MowLib creative tab.
- `tint/` - colourable blocks
  - `IColorableBlock`: implement on a block to make it dyeable. Tint regions (`getTintRegionCount`,
    `getTintRegion` from the clicked point, `getRegionForTintIndex`, `getAddedRegion` for slabs becoming doubles),
    `getDefaultColor(region)`, `tryDyeBlock` (leather-armour colour mixing; dyes are not used up), `dyeColorOf`
    (vanilla dyes + Custom Dye). Extends `EntityBlock`: by default the block gets a `CBTintBlockEntity`.
  - `ITintHolder`: what dyeing, the picker and the tint sources read colours from.
  - `CBTintData`: per-region colour storage (save keys "color", "color1", ...; client re-mesh; item `dyed_color`
    in and out). Block entities that extend a vanilla class (chests, barrels) own one and return false from
    `usesTintBlockEntity()`.
  - `CBTintBlockEntity` (`mowlib:tint_block_entity`): the plain block entity, a `CBTintData` plus slab handling.
- `client/` - client-only. `CBBlockEntityTintSource` (one tint source per region for every `IColorableBlock`,
  registered by `MowLibClient`; a model face's `tintindex` = region; mods must not register their own),
  `MowLibKeyMappings` + `PageInputHandler` (page keys and scrolling), `ClientPagedContentsTooltip`,
  `PagedFullnessProperty` (`mowlib:paged_fullness`), `SpinningItemRenderer`, `GhostItemRenderer`,
  `MowLibClientParticles`.
- `storage/`
  - `CBBulkItemHandler`: NeoForge `ResourceStacksResourceHandler<ItemResource>`. Each slot holds one item type and
    an int amount up to `capacity` (never an `ItemStack`, those cap at 99 when saved). Saved via
    `serialize`/`deserialize` (key "stacks").
  - `CBBulkStorageBlock` (+ `CBBulkStorageBlockEntity`, shared type `mowlib:bulk_storage`): right-click inserts,
    left-click takes 1, sneak-left-click a stack; creative players extract until empty before breaking; spills on
    break; comparator output; tintable. Subclass with shape, tint regions, capacity and item tag.
  - `StorageInteractions`: the shared insert / take / give-to-player / fluid container / filter slot clicks. Use
    these rather than writing new ones. `ItemEntityAccess`: an item entity used like an inventory slot.
- `transport/` - `TransportType` registry (`mowlib:transport_type`; items/fluids/energy/experience built in). A
  type = how to store, reach (block/entity/item handlers), save, filter, drop, absorb and show one kind of thing;
  machine numbers (rates, sizes) belong to the machine (`TransportStorageHost#getCapacity`). `TransportStorage`,
  `AmountResource`/`AmountStorage` for amount-only types, `TransportContents` (`mowlib:transport_contents`).
  `ExperienceTransportType` makes the `mowlib:experience_handler` block + item capabilities.
- `filter/` - `ITransportFilter`, `FilterData` (`mowlib:filter_data`, `mowlib:filter_mode`), `FilterDirection`,
  `IFilterHolder` (block entities with a filter slot). `items/filters/`: the 11 filters (`mowlib:filter_*`) on
  `BaseFilterItem`; models are hand-written in `assets/mowlib/items` (one per transport mode).
- `workcard/` - `IWorkCard`, `IWorkCardHolder`, `WorkCardType`. `items/workcards/`: area + locations cards
  (`mowlib:work_positions`).
- `items/` - `CBColorPickerItem` (`mowlib:color_picker`; picked colour from `ITintHolder` or the
  `mowlib:block_colors` data map), `mowlib:custom_dye`. `items/tools/`: `MowLibToolItem` (offhand tools with a
  tooltip), `FilterToolItem`, `CraftingToolItem` (stays in the grid, loses durability).
- `palette/` - `ColorPaletteItem` (`mowlib:color_palette`): bundle-like, one of each dye (vanilla + Custom Dye; a
  duplicate is used up and voided), sorted by colour, max 64. The selected dye (`IPageable`: scroll / page keys) is
  what it paints with (`tint/IDyeSource`), and tints its model (`client/SelectedDyeTint`, `mowlib:selected_dye`).
  `ColorPaletteContents` (`mowlib:color_palette`), `ColorPaletteTooltip` + `client/ClientColorPaletteTooltip`.
- `recipes/` - `CBPickedDyeRecipe` (`mowlib:picked_dye`: picker alone -> custom dye named after the picked block, `mowlib:picked_block`), `ColorableItemDyeRecipe`
  (`mowlib:colorable_dye`: `#mowlib:dyeable_in_crafting` + dyes -> mixed `dyed_color`).
- `datamaps/` - `CBBlockColor` (`{"color": "#RRGGBB"}`), `CBDataMaps.BLOCK_COLORS`.
- `datagen/` - models (picker, dye), language, recipes, and `CBBlockColorProvider` (average texture colour of
  plain solid cubes).
- `paging/` - pages on items (`IPageable`) and block entities (`IPageHolder`), turned for every mod by scroll or
  the page keys. `PageArrows` (clickable page-turn boxes). Paged storage: `PagedStorage` (block entities),
  `IPagedStorageItem` + `PagedStorageInteractions` (bundle-style clicks), `PagedContents`
  (`mowlib:paged_contents`), `PagedContentsTooltip`.
- `network/` - `MowLibNetwork` registers the payloads: `select_page`, `turn_held_page`, `turn_block_page`
  (client -> server), `transfer_particles` (server -> client). Bump its `VERSION` when a payload changes.
- `knapping/` - hold items in both hands and right-click a block. Mods register `KnappingRecipe`s in
  `KnappingRecipes` from common setup; `KnappingHandler` runs them.
- `drops/` - bonus drops when a player breaks a block. Mods register `BonusDrop`s in `BonusDrops` from common
  setup; `BonusDropHandler` rolls every match.
- `display/ISpinningItemHolder` + `client/SpinningItemRenderer`: a block entity's item spinning above it. Mods
  register the renderer for their own types (`SpinningItemRenderer::new`).
- `ghost/GhostItem` + `client/GhostItemRenderer`: floating "missing input" items with labels.
- `util/` - `CBPixelBox` (pixel boxes: rotate incl. `rotateFromUp` for six-facing models, shapes, click-to-pixel),
  `MowLibOwner` + `MowLibFakePlayer`, `MowLibBlockActions` (break/place as a player), `MowLibXpUtils`,
  `MowLibTextUtils`, `MowLibParticles`.

## Releases
- Semver in `gradle.properties` `mod_version`: patch = fixes only, minor = new features (only additions),
  major = anything breaking (removed/renamed public class, method or registry id; changed signature, save format or
  behaviour mods rely on).
- Within a major version only add things. When something is replaced, keep the old method/class working and mark
  it `@Deprecated` (with a comment saying what to use instead); remove it in the next major.
- Why this matters: Jar-in-Jar loads only the newest MowLib bundled by any installed mod. A breaking change in that
  newest copy breaks every older mod installed alongside it. Mods declare a range that stops before the next major
  (e.g. `[1.2.0,2.0.0)`).
- Steps (I do these; Claude never commits, pushes or tags): bump `mod_version`, commit and push, tag `vX.Y.Z` and
  push the tag, then `./gradlew publishToMavenLocal` (published as `com.mowmaster.mowlib:mowlib:X.Y.Z` in
  `~/.m2`). Then update the version in each mod that needs the change.

## 26.x notes (learned in CrystalBlocks)
- Render layer comes from texture alpha; use `noOcclusion()` on see-through blocks.
- Datagen: models via `ModelProvider`/`BlockModelGenerators` (override `getKnownItems`/`getKnownBlocks` to list
  what it must cover); recipes and loot via `GatherDataRegistryEntriesEvent`. Recipe ids are full ids
  (`"mowlib:picked_dye"`); a bare name lands in `minecraft:`.
- Block entity save/load uses `ValueOutput`/`ValueInput`; `updateShape` gets a `ScheduledTickAccess`;
  `useItemOn` returns `InteractionResult.SUCCESS / PASS / TRY_WITH_EMPTY_HAND`.
- Item transfer API: `ResourceHandler<ItemResource>`, `Transaction.openRoot()`, `VanillaContainerWrapper`.
- Give items to players with `StorageInteractions.giveToPlayer` (`placeItemBackInInventory`), not `Inventory#add`,
  which silently keeps what doesn't fit.

## Model conventions
- Texture slots `#texture_one/two/three`; `cullface` only on faces at the block edge; glowing elements use
  `"light_emission": 15` + `"neoforge_data": {"ambient_occlusion": false}`.

## Working with Claude
- Pull and read the repo only; never commit or push. I handle git.
- Deliver changes as a zip of whole files at their repo paths, plus a list of files to delete.
- For simple model fixes (a missing `tintindex`, `#`, texture key), just tell me the line to change; only send a model file when the geometry changes.
- To check 26.3 APIs, read the public `neoforged/neoforge` repo (branch `26.3.x`) and the porting primers in `neoforged/.github` (`primers/26.1`, `26.2`, `26.3`). NeoForge's docs are in `neoforged/Documentation`.
- The mods using MowLib: `Mowmaster/CrystalBlocks`, `Mowmaster/Pedestals`, `Mowmaster/Bibliomania` (branch `NeoForge-26x`).
