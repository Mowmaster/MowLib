package com.mowmaster.mowlib.drops;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

// Every mod's bonus drops, used by BonusDropHandler. They're code, not data: register them from your mod's
// FMLCommonSetupEvent (every item exists by then):
//
//     event.enqueueWork(() -> BonusDrops.register(BonusDrop.of(MyTags.KNIVES, MyTags.GRASS_PLANTS, 0.2f,
//             () -> new ItemStack(MyItems.FIBERS.get()))));
//
// Unlike knapping, every matching bonus drop rolls, so several mods can add drops to the same block.
public final class BonusDrops
{
    private BonusDrops() {}

    // CopyOnWriteArrayList: safe to read from the game thread while a mod is still adding to it during setup
    private static final List<BonusDrop> DROPS = new CopyOnWriteArrayList<>();

    public static void register(BonusDrop drop)
    {
        DROPS.add(drop);
    }

    // A read-only view (no copy: this runs on every block break). Looping over a CopyOnWriteArrayList never sees a
    // half-finished change.
    public static List<BonusDrop> all()
    {
        return Collections.unmodifiableList(DROPS);
    }
}
