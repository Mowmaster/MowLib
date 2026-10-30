# Switching CrystalBlocks to MowLib

Do this in a CrystalBlocks session once MowLib builds and is published (even just to a local repo). Nothing here has
been applied to CrystalBlocks yet.

## 1. Depend on MowLib

`build.gradle` (CrystalBlocks):

```gradle
repositories {
    // While developing: MowLib's `publish` task writes to MowLib/repo. Point this at that folder,
    // or use mavenLocal() with `publishToMavenLocal`.
    maven { url = uri("file:///path/to/MowLib/repo") }
}

dependencies {
    // Compile against MowLib and bundle it inside the CrystalBlocks jar (Jar-in-Jar)
    jarJar(implementation(group: 'com.mowmaster.mowlib', name: 'mowlib')) {
        version {
            strictly '[0.0.1,1.0.0)'
            prefer '0.0.1'
        }
    }
}
```

`src/main/templates/META-INF/neoforge.mods.toml`, add:

```toml
[[dependencies.${mod_id}]]
    modId="mowlib"
    type="required"
    versionRange="[0.0.1,1.0.0)"
    ordering="AFTER"
    side="BOTH"
```

## 2. Delete the copies that now live in MowLib

- `Blocks/Colorable/`: `IColorableBlock`, `ITintHolder`, `CBTintData`, `CBTintBlockEntity`, `CBPixelBox`
- `Blocks/Storage/`: `CBBulkItemHandler`, `CBBulkStorageBlock`, `CBBulkStorageBlockEntity` (keep `CBIngotPile`, `CBLogPile`)
- `Items/Tools/CBColorPickerItem`, `Recipes/CBPickedDyeRecipe`, `Recipes/CBRecipeRegistry` (nothing else is in it)
- `DataMaps/` (all of it), `ClientSide/CBBlockEntityTintSource`
- Textures `textures/item/color_picker.png`, `color_picker_dyed.png`

## 3. Remove what MowLib now registers or does

- `CBItemRegistry`: `COLOR_PICKER`, `CUSTOM_DYE`
- `CBBlockEntityRegistry`: `TINT_BLOCK_ENTITY`, `BULK_STORAGE` and their `colorableBlocks()` / `bulkStorageBlocks()`
  helpers (keep the chest and barrel types)
- `CBCapabilities`: the `BULK_STORAGE` line (keep chest and barrel)
- `CrystalBlocksClient`: `registerTintBlocks` (MowLib registers tint sources for every mod's blocks)
- `CrystalBlocks`: `CBRecipeRegistry.register(...)` and the picker/dye creative tab entries
- Datagen: the picker and custom dye item models (`CBBasicModelProvider`), their lang lines and the two
  `message.crystalblocks.color_picker.*` lines (`CBLanguageProvider`), the `picked_dye` special recipe
  (`CBBasicRecipeProvider`), and the `CBBlockColorProvider` line in `CBDataGenerator`
- Then rerun datagen so the old generated files disappear

## 4. Fix imports

| Old | New |
|---|---|
| `com.mowmaster.crystalblocks.Blocks.Colorable.IColorableBlock` / `ITintHolder` / `CBTintData` / `CBTintBlockEntity` | `com.mowmaster.mowlib.tint.*` |
| `com.mowmaster.crystalblocks.Blocks.Colorable.CBPixelBox` | `com.mowmaster.mowlib.util.CBPixelBox` |
| `com.mowmaster.crystalblocks.Blocks.Storage.CBBulk*` | `com.mowmaster.mowlib.storage.CBBulk*` |
| `com.mowmaster.crystalblocks.Items.Tools.CBColorPickerItem` | `com.mowmaster.mowlib.items.CBColorPickerItem` |
| `CBItemRegistry.COLOR_PICKER` / `CUSTOM_DYE` | `com.mowmaster.mowlib.registry.MowLibItems.COLOR_PICKER` / `CUSTOM_DYE` |

Classes in `Blocks/Colorable` that used `CBPixelBox`, `IColorableBlock` etc. without an import (same package before)
now need the imports.

## 5. Keep existing worlds working (registry aliases)

Saved worlds refer to `crystalblocks:tint_block_entity`, `crystalblocks:bulk_storage`, `crystalblocks:color_picker`
and `crystalblocks:custom_dye`. Without aliases, colours, pile contents and those items are lost. Add right after
each `DeferredRegister` is created (aliases must be added before registration):

```java
// CBBlockEntityRegistry
static {
    CBBLOCKENTITIES.addAlias(Identifier.fromNamespaceAndPath(CBReferences.MODID, "tint_block_entity"),
            Identifier.fromNamespaceAndPath("mowlib", "tint_block_entity"));
    CBBLOCKENTITIES.addAlias(Identifier.fromNamespaceAndPath(CBReferences.MODID, "bulk_storage"),
            Identifier.fromNamespaceAndPath("mowlib", "bulk_storage"));
}

// CBItemRegistry
static {
    CBITEMS.addAlias(Identifier.fromNamespaceAndPath(CBReferences.MODID, "color_picker"),
            Identifier.fromNamespaceAndPath("mowlib", "color_picker"));
    CBITEMS.addAlias(Identifier.fromNamespaceAndPath(CBReferences.MODID, "custom_dye"),
            Identifier.fromNamespaceAndPath("mowlib", "custom_dye"));
}
```

An alias only applies when the old name is no longer registered, which is the case once the originals are deleted.

## 6. Test

- An old world: dyed blocks keep their colours, piles keep their contents, pickers and custom dyes in chests survive.
- New: dyeing, the picker (including picking from vanilla blocks), custom dye crafting, piles, hoppers into piles.
