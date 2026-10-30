package com.mowmaster.mowlib.datagen;

import com.mowmaster.mowlib.MowLibReferences;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackType;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.data.event.GatherDataRegistryEntriesEvent;

@EventBusSubscriber(modid = MowLibReferences.MODID)
public class MowLibDataGenerator
{
    @SubscribeEvent
    public static void gatherClientData(GatherDataEvent.Client event)
    {
        DataGenerator generator = event.getGenerator();
        PackOutput packOutput = generator.getPackOutput();
        var lookupProvider = event.getReloadableLookupProvider();

        generator.addProvider(true, new MowLibModelProvider(packOutput));
        generator.addProvider(true, new MowLibLanguageProvider(packOutput));
        // Average colours of plain vanilla blocks, for the colour picker
        generator.addProvider(true, new CBBlockColorProvider(packOutput, lookupProvider,
                event.getResourceManager(PackType.CLIENT_RESOURCES)));
    }

    @SubscribeEvent
    public static void gatherDataRegistryEntries(GatherDataRegistryEntriesEvent event)
    {
        event.gatherFor(MowLibReferences.MODID, Identifier.DEFAULT_NAMESPACE)
                .add(MowLibRecipeProvider.create());
    }
}
