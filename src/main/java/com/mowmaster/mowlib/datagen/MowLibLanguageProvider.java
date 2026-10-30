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
    }
}
