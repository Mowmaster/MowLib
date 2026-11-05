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
- Root: `MowLib` (registers everything), `MowLibClient` (client registrations, the client config), `MowLibReferences`
  (`MODID`, `id(path)`), `MowLibCapabilities` (bulk storage item capability).
- `blockentity/CBSyncedBlockEntity`: base for block entities whose saved data is also the client's copy (`sync()`,
  `getUpdateTag`, `getUpdatePacket`). `CBTintBlockEntity` extends it.
- `block/HorizontalFacing`: static helpers for four-way `FACING` blocks (placement towards/away from the player,
  `rotate`, `mirror`, `quarterTurns`).
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
  `MowLibKeyMappings` + `PageInputHandler` (page keys and scrolling; a held `keys/IPageKeyTool` gets them first),
  `KeyActionHandler` (the activate / place / pick-up keys), `HeldPageMessages` (page turned in hand -> message over
  the hotbar), `ClientPagedContentsTooltip`, `PagedFullnessProperty` (`mowlib:paged_fullness`),
  `SpinningItemRenderer`, `GhostItemRenderer`, `MowLibClientParticles`, `CBItemRenderer` (base for item-drawing block
  entity renderers), `FaceItemRenderer` (an item flat on a block face), `HudAnchor` (nine screen anchors for HUD
  panels), `MowLibClientConfig` (`mowlib-client.toml`: where the reading HUD's panels go), `SlotGrid` (drawing grids
  of item slots: tooltips, HUD), `ClientLook` (looked-at block, hovered slot).
  `client/reading/ReadingHud`: the reading HUD (see `reading/`).
- `storage/`
  - `CBBulkItemHandler`: NeoForge `ResourceStacksResourceHandler<ItemResource>`. Each slot holds one item type and
    an int amount up to `capacity` (never an `ItemStack`, those cap at 99 when saved). Saved via
    `serialize`/`deserialize` (key "stacks").
  - `CBBulkStorageBlock` (+ `CBBulkStorageBlockEntity`, shared type `mowlib:bulk_storage`): right-click inserts,
    left-click takes 1, sneak-left-click a stack; creative players extract until empty before breaking; spills on
    break; comparator output; tintable. Subclass with shape, tint regions, capacity and item tag.
  - `StorageInteractions`: the shared insert / take / give-to-player / fluid container / filter slot clicks, plus
    `withSlotAccess` / `cursorAccess` (inventory slots and the cursor as `ItemAccess`) and `move` (between two
    handlers of a type). Use these rather than writing new ones. `ItemEntityAccess`: an item entity used like an
    inventory slot.
  - `IInventoryUsable` + `InventoryClicks` + `InventoryClick`: items other items are used on in an inventory, either
    way round (food onto a mob book, seeds onto a planter); `useHeld` for a block standing in for the item. Every
    bundle-style item goes through it (paged storage items, the Color Palette); `usedOnEmptySlots` for "take one out
    into this slot"; shared bundle sounds.
- `transport/` - `TransportType` registry (`mowlib:transport_type`; items/fluids/energy/experience built in). A
  type = how to store, reach (block/entity/item handlers), save, filter, drop, absorb and show one kind of thing;
  machine numbers (rates, sizes) belong to the machine (`TransportStorageHost#getCapacity`). `TransportStorage`,
  `AmountResource`/`AmountStorage` for amount-only types, `TransportContents` (`mowlib:transport_contents`).
  Amounts are written one way: `TransportType#amountText` (unit) and `describeStored` ("Water: 1000 mB / 16000 mB").
  `ExperienceTransportType` makes the `mowlib:experience_handler` block + item capabilities.
  Items holding one type (tanks, batteries, tomes): `ITransportStorageItem`, `ItemTransportStorage` (opens the
  item's `mowlib:transport_contents` as a storage), `TransportItemInteractions` (bucket-style inventory clicks with
  any item of the type, fill bar).
- `filter/` - `ITransportFilter`, `FilterData` (`mowlib:filter_data`, `mowlib:filter_mode`), `FilterDirection`,
  `IFilterHolder` (block entities with a filter slot), `HeldFilters` (a filter inside an item, `mowlib:held_filter`:
  get/set/allows, the offhand filter / Filter Tool clicks for `use()`, a whitelist summary for paged items). Held and
  block filters follow the same rules (`ITransportFilter.allowsMoving` / `limitStored`): a filter only acts in the
  direction it's set to. `items/filters/`: the 13 filters (`mowlib:filter_*`) on `BaseFilterItem`; a mode's list is set
  bundle-style in an inventory (click items onto the filter; nothing is used up). `TagFilterItem` (`filter_tag`,
  `filter_tag_machine`) matches the selected tag of each `TagGetterItem` (`mowlib:tag_getter`, data
  `filter/TagGetterData`) on its list. Models are hand-written in `assets/mowlib/items` (one per transport mode).
- `workcard/` - `IWorkCard`, `IWorkCardHolder`, `WorkCardType`. `items/workcards/`: area + locations cards
  (`mowlib:work_positions`).
- `items/` - `CBColorPickerItem` (`mowlib:color_picker`; picked colour from `ITintHolder` or the
  `mowlib:block_colors` data map), `mowlib:custom_dye`. `items/tools/`: `MowLibToolItem` (offhand tools with a
  tooltip; comes back out of crafting grids), `FilterToolItem`, `CraftingToolItem` (stays in the grid, loses
  durability).
- `palette/` - `ColorPaletteItem` (`mowlib:color_palette`): bundle-like, one of each dye (vanilla + Custom Dye; a
  duplicate is used up and voided), sorted by name, max 64. The selected dye (`IPageable`: scroll / page keys) is
  what it paints with (`tint/IDyeSource`), and tints its model (`client/SelectedDyeTint`, `mowlib:selected_dye`).
  `ColorPaletteContents` (`mowlib:color_palette`), `ColorPaletteTooltip` + `client/ClientColorPaletteTooltip`.
- `recipes/` - `CBPickedDyeRecipe` (`mowlib:picked_dye`: picker alone -> custom dye named after the picked block, `mowlib:picked_block`), `ColorableItemDyeRecipe`
  (`mowlib:colorable_dye`: `#mowlib:dyeable_in_crafting` + dyes -> mixed `dyed_color`).
- `datamaps/` - `CBBlockColor` (`{"color": "#RRGGBB"}`), `CBDataMaps.BLOCK_COLORS`.
- `datagen/` - models (picker, dye), language, recipes, and `CBBlockColorProvider` (average texture colour of
  plain solid cubes).
- `paging/` - pages on items (`IPageable`) and block entities (`IPageHolder`), turned for every mod by scroll or
  the page keys. `PageArrows` (clickable page-turn boxes). Paged storage: `PagedStorage` (block entities),
  `IPagedStorageItem` + `PagedStorageInteractions` (bundle-style clicks on `InventoryClicks`; `allowsTakeOut` can
  refuse a page), `PagedContents` (`mowlib:paged_contents`), `PagedContentsTooltip`. `PageText`: the one wording for
  pages ("Page 3/8: 12 Bread") in the tooltip, hotbar message and reading HUD.
- `keys/` - the action keys' item/block side: `IActivatable` (activate key on a hovered or held item; the on/off
  mode in `mowlib:activated`), `IKeyPlaceable` (place key puts a block item down, skipping its right-click),
  `IPickupableBlock` (pick-up key, and any block's own pickup click: block -> item with contents), `IPageKeyTool`
  (a held tool that takes the page keys at certain blocks), `KeyPayloads`.
- `reading/` - the reading HUD for every mod, while `ReadingSight.canRead` (a helmet in `#mowlib:grants_reading`,
  or one with an enchantment in the enchantment tag `#mowlib:grants_reading`, or an added condition):
  - the paper: `IReadableBlock` (block entity or block) returns a `ReadingPaper` (lines, optional page grid,
    optional server lines from `IServerReadable`); `IReadableItem` for held items and items shown by blocks;
    `ReadingPapers` has the common papers (an item's, a paged item's) and lines (`hud.mowlib.*`)
  - the label: any other block or mob, with what the server says it holds (`ReadingLabels`)
  - `ReadingRegistry`: readers and label lines for blocks/mobs you can't implement an interface on
  - `ReadingPayloads`: the server questions
- `network/` - `MowLibNetwork` registers the payloads: `select_page`, `turn_held_page`, `turn_block_page`,
  `activate`, `place_held`, `pickup_block`, `page_key_tool`, `reading_block_request`, `reading_entity_request`
  (client -> server), `transfer_particles`, `reading_block`, `reading_entity` (server -> client). Bump its
  `VERSION` when a payload changes.
- `knapping/` - hold items in both hands and right-click a block. Mods register `KnappingRecipe`s in
  `KnappingRecipes` from common setup; `KnappingHandler` runs them.
- `drops/` - bonus drops when a player breaks a block. Mods register `BonusDrop`s in `BonusDrops` from common
  setup; `BonusDropHandler` rolls every match.
- `display/ISpinningItemHolder` + `client/SpinningItemRenderer`: a block entity's item spinning above it. Mods
  register the renderer for their own types (`SpinningItemRenderer::new`).
- `ghost/GhostItem` + `client/GhostItemRenderer`: floating "missing input" items with labels.
- `display/MobDisplay` + `client/MobDisplayRenderer`: a mob shown on a block (spin / face player / fixed; baby,
  adult or a growing-up loop; sheep colour and sheared; on fire). Use the renderer inside your own block entity renderer, or
  `display/IMobDisplayHolder` + `client/MobDisplayBlockRenderer`. `#mowlib:mob_display_blacklist`: mobs never shown.
- `util/` - `CBPixelBox` (pixel boxes: rotate incl. `rotateFromUp` for six-facing models, shapes, click-to-pixel),
  `MowLibOwner` + `MowLibFakePlayer`, `MowLibBlockActions` (break/place as a player), `MowLibXpUtils`,
  `MowLibTextUtils`, `MowLibParticles`, `LeftClickCooldown` (keeps cancelled left-click block actions to
  right-click's pace).

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
