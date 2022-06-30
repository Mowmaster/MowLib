package com.mowmaster.mowlib.Registry;

import com.mowmaster.mowlib.MowLibUtils.ColorReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ColorHandlerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "mowlib", bus = Mod.EventBusSubscriber.Bus.MOD)
public class MowLibClientRegistry
{

    @SubscribeEvent
    public static void registerItemColor(ColorHandlerEvent.Item event) {

        event.getItemColors().register((stack, color) ->
        {if (color == 1) {return ColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.COLOR_APPLICATOR.get());
        event.getItemColors().register((stack, color) ->
        {if (color == 1) {return ColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.ICON_DUST.get());
        event.getItemColors().register((stack, color) ->
        {if (color == 1) {return ColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.ICON_EFFECT.get());


    }

    /*@SubscribeEvent
    public static void registerBlockColor(ColorHandlerEvent.Block event) {

        event.getBlockColors().register((blockstate, blockReader, blockPos, color) ->
        {if (color == 1) {return ColorReference.getColorFromStateInt(blockstate);} else {return -1;}}, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
    }*/
}
