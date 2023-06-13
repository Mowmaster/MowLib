package com.mowmaster.mowlib.Registry;

import com.mowmaster.mowlib.MowLibUtils.MowLibReferences;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

public class DeferredCreativeTabRegistry
{
    public static final DeferredRegister<CreativeModeTab> DEF_REG = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MowLibReferences.MODID);

    public static final RegistryObject<CreativeModeTab> TAB = DEF_REG.register(MowLibReferences.MODID, () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup." + MowLibReferences.MODID))
            .icon(() -> makeIcon())
            .displayItems((enabledFeatures, output) -> {
                for(RegistryObject<Item> item : DeferredRegisterItems.ITEMS.getEntries()){
                    output.accept(item.get());
                }
            })
            .build());

    private static ItemStack makeIcon() {
        ItemStack stack = new ItemStack(DeferredRegisterItems.COLOR_APPLICATOR.get());
        CompoundTag tag = new CompoundTag();
        tag.putInt("3DRender", 1);
        stack.setTag(tag);
        return stack;
    }
}
