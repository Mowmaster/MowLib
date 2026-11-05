# MowLib changelog

What changed in each alpha, and what a mod using MowLib should check when it updates. Newest first.
"Check" lines are what can break or change behaviour in a mod; "New" lines are additions a mod may want to use.
Changes go under **Unreleased** as they're made; when `mod_version` is bumped, that section gets the version's name.

## Unreleased (after 0.0.4)

### Check
- **Knapping deprecated** (`knapping/`: `KnappingRecipe`, `KnappingRecipes`, `KnappingHandler`). It still works
  for now but will be removed; move recipes to Dual Handed recipes: JSON in
  `data/<namespace>/mowlib/dual_handed_recipe/` (see `recipes/DualHandedRecipe`). Uses of `KnappingRecipes.register`
  show deprecation warnings.

## 0.0.4

### Check
- **Work card crafting recipes removed** (`workcards/area_card`, `workcards/location_card`). Work cards now come from
  the Blank Work Card's picker menu (Work Card Tool + 8 sticks -> Blank Work Card).
- **Filters: setting the list changed.** Sneak + right-clicking a container no longer sets a filter's list; items are
  clicked onto the filter in an inventory (bundle-style, nothing used up). Tell players / update docs.
- **`HeldFilters` follows the filter's direction**, like block filters: `allows` / `holderAllows` without a direction
  mean INSERT; an EXTRACT or NEUTRAL filter no longer restricts what goes into the item. Use
  `holderAllows(holder, type, resource, direction)` / `holderAllowsExtract` / `holderLimit` where needed.
- **`FilterData.Entry` has a `selected` field** (the list item picked by scroll / page keys). The old 2-value
  constructor still works.
- **`MobDisplay` has an `onFire` field** (`.burning(true)`). The old 7-value constructor still works.
- **`MobDisplayRenderer` lighting:** the mob is lit by the world at its feet by default. `extract(...)` without a
  light argument is the normal call; passing a block entity render state's `lightCoords` makes it dark (that's the light
  inside the block). `MobDisplayRenderer.WORLD_LIGHT` (-1) = use the world. No shadow is drawn any more.
- **Page wording** is one format everywhere ("Page 3/8: 12 Bread", `paging/PageText`). Removed lang keys:
  `tooltip.mowlib.paged.page`, `tooltip.mowlib.paged.empty_page`, `message.mowlib.page`, `message.mowlib.page_empty`,
  `message.mowlib.page_item`, `hud.mowlib.page`, `hud.mowlib.page_item`, `hud.mowlib.empty_page`.
- **Stored amounts wording:** `TransportStorage#describe()` now reads "Water: 1000 mB / 16000 mB",
  "Energy: 500 FE / 10000 FE" (`TransportType#describeStored`). Custom transport types can override `amountText`.
- **`PagedStorageInteractions`** runs through `InventoryClicks` (same method signatures). A click onto a slot the
  player may not change now does the normal vanilla click instead of trying to store into it.
- **`CBPixelBox.quarterTurns` deprecated**: use `HorizontalFacing.quarterTurns`.
- **`CBTintBlockEntity` now extends `CBSyncedBlockEntity`** (same client data). Subclasses overriding
  `getUpdateTag` / `getUpdatePacket` still work.
- **`InventoryClick` / `InventoryClicks`** use `net.minecraft.world.entity.SlotAccess` (the earlier
  `net.minecraft.world.SlotAccess` import didn't compile).
- **Turning a looked-at block's pages by key** now needs the player's real reach and permission to change the block
  (`isWithinBlockInteractionRange` + `mayInteract`), instead of a fixed 8 blocks.
- **Network version 6**: client and server must run the same MowLib.
- **Color Palette** sorts its dyes by name (A to Z), not colour.

### New
- From Bibliomania: `blockentity/CBSyncedBlockEntity`, `block/HorizontalFacing`, `client/CBItemRenderer`,
  `client/FaceItemRenderer`, `client/HudAnchor`, the reading HUD (`reading/`, `client/reading/ReadingHud`,
  `#mowlib:grants_reading` item and enchantment tags), `client/MowLibClientConfig` (`mowlib-client.toml`), the action
  keys (`keys/`: `IActivatable`, `IKeyPlaceable`, `IPickupableBlock`, `IPageKeyTool`; `client/KeyActionHandler`),
  `storage/IInventoryUsable` + `InventoryClicks` + `InventoryClick`, `transport/ITransportStorageItem` +
  `ItemTransportStorage` + `TransportItemInteractions`, `filter/HeldFilters` (`mowlib:held_filter`),
  `util/LeftClickCooldown`, `IPagedStorageItem#allowsTakeOut`, `StorageInteractions.withSlotAccess` / `cursorAccess` /
  `move`.
- **Picker groups** (`picker/`): blank items that turn into one of a set through a radial menu. `ItemPicker.register`,
  `ItemPicker.open` (server) / `openOnClient` (client), `items/PickerBaseItem`, `client/ItemPickerScreen`,
  `recipes/PickerResetRecipe` (`mowlib:picker_reset`: any group item alone -> its blank). MowLib's groups: Blank
  Filter (`mowlib:filter_base`, Filter Tool + 8 sticks) and Blank Work Card (`mowlib:workcard_base`, Work Card Tool +
  8 sticks). Client config `[picker_menu] ring_max`.
- **Dual Handed crafting** (`recipes/DualHandedRecipe` + `DualHandedCrafting`): offhand item + main hand item +
  right-clicked block (each optionally used up) -> an item; data-driven (synced world datapack registry, item and block
  tags allowed). MowLib's: Filter Tool (paper + stick on a bookshelf), Work Card Tool (coal + stick on a bookshelf).
- **Tag Getter and tag filters** (`mowlib:tag_getter`, `mowlib:filter_tag`, `mowlib:filter_tag_machine`; data
  `filter/TagGetterData`): the getter reads an item's tags (clicked onto it) or a block's block tags (pick-up key).
- **Filter keys:** page keys while holding switch mode; activation key toggles whitelist/blacklist; pick-up / place keys
  add / remove the looked-at block (`keys/IBlockKeyTool`, `TransportType#filterEntryAt`). Scroll / page keys over a
  filter select a list item; right-click removes the selected one. Blacklist tint on the frame
  (`mowlib:filter_list`), Allowed / Disabled with the plain icon for amount-only modes (`TransportType#isAmountOnly`,
  `mowlib:filter_blacklist`).
- `IPageable#describePage`, `#turnHeld`, `#showsHeldPageMessages`; `IInventoryUsable#usedOnEmptySlots`;
  `InventoryClick#take` / `#putTaken`; `client/ClientActivations` (client-side activation-key actions);
  `client/SlotGrid`; `client/ClientLook`; `storage/TwoSlotItemAccess`; `MowLibTextUtils.effectName`;
  `ITransportFilter.applies` / `allowsMoving` / `limitStored`; `TransportType#amountText` / `describeStored`.
- `MowLibToolItem` (Filter Tool, Work Card Tool, subclasses) comes back out of crafting grids.
- `MobDisplay` can show a burning mob.

## 0.0.3

### New
- **Mob display:** `display/MobDisplay` (which mob; spin / face player / fixed; baby, adult or growing up; sheep colour
  and sheared; size), `client/MobDisplayRenderer` (use inside your own block entity renderer),
  `display/IMobDisplayHolder` + `client/MobDisplayBlockRenderer`. `#mowlib:mob_display_blacklist` entity tag.

## 0.0.2

### Check
- `IColorableBlock.dyeColorOf` asks `tint/IDyeSource` items first (the Color Palette dyes with its selected dye).

### New
- **Color Palette** (`mowlib:color_palette`): holds one of each dye, paints with the selected one, tints its model
  (`mowlib:selected_dye`).
- Custom Dyes crafted from a colour picker are named after the picked block (`mowlib:picked_block`).

## 0.0.1

First alpha: tinting (`tint/`), bulk and paged storage, transport types, filters, work cards, paging, ghost items,
spinning items, bonus drops, the colour picker and Custom Dye, block colour data map, utilities.

### Check (compared with the code the mods had before MowLib)
- Registry ids are under `mowlib:`; mods moving things here keep old worlds loading with registry aliases.
- Shared block entity types (`mowlib:tint_block_entity`, `mowlib:bulk_storage`) find every mod's blocks themselves;
  mods don't register them.
- `CBBulkItemHandler.insertStack` / `extractStack` are deprecated: use `StorageInteractions`.
- Items given to players go through `StorageInteractions.giveToPlayer` (drops what doesn't fit; `Inventory#add` lost
  it).
