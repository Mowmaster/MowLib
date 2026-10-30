package com.mowmaster.mowlib.datagen;

import com.mowmaster.mowlib.MowLibReferences;
import com.mowmaster.mowlib.registry.MowLibItems;
import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.LanguageProvider;

public class MowLibLanguageProvider extends LanguageProvider
{
    public MowLibLanguageProvider(PackOutput output)
    {
        super(output, MowLibReferences.MODID, "en_us");
    }

    @Override
    protected void addTranslations()
    {
        addItem(MowLibItems.COLOR_PICKER, "Color Picker");
        addItem(MowLibItems.CUSTOM_DYE, "Custom Dye");
        add("message.mowlib.color_picker.picked", "Picked color %s");
        add("message.mowlib.color_picker.none", "This block has no color to pick");

        // Page keys (client/MowLibKeyMappings)
        add("key.category.mowlib.mowlib", "MowLib");
        add("key.mowlib.page_next", "Next Page");
        add("key.mowlib.page_previous", "Previous Page");
        // Paged storage tooltip (client/ClientPagedContentsTooltip)
        add("tooltip.mowlib.paged.page", "Page %1$s/%2$s: %3$s");
        add("tooltip.mowlib.paged.empty_page", "Empty");
        add("tooltip.mowlib.paged.fullness", "Holds %1$s / %2$s");


    }
}
