package com.mowmaster.mowlib.Registry;

import com.mowmaster.mowlib.Items.Filters.FilterRestricted;
import com.mowmaster.mowlib.MowLibUtils.MowLibColorReference;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterColorHandlersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = "mowlib", bus = Mod.EventBusSubscriber.Bus.MOD)
public class MowLibClientRegistry
{

    @SubscribeEvent
    public static void registerItemColor(RegisterColorHandlersEvent.Item event) {

        event.getItemColors().register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.COLOR_APPLICATOR.get());
        event.getItemColors().register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.ICON_DUST.get());
        event.getItemColors().register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.ICON_EFFECT.get());

        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ITEM.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ITEMSTACK.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_DURABILITY.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED_COUNT.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED_EXACT.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_ENCHANTED_FUZZY.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_FOOD.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_MOD.get());
        event.register((stack, color) ->
        {if (color == 1) {return FilterRestricted.getColor(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_RESTRICTED.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_TAG.get());

        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        event.register((stack, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromItemStackInt(stack);} else {return -1;}}, DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());



        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_ITEM.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_ITEMSTACK.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_DURABILITY.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED_COUNT.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED_EXACT.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_ENCHANTED_FUZZY.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_FOOD.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_MOD.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_RESTRICTED.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_TAG.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_TAG_MACHINE.get());

        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_EXACT.get());
        ItemModelPropertiesMowLib.dustItemModes(DeferredRegisterItems.FILTER_BLOCKS_ON_CLICK_FUZZY.get());

    }

    /*@SubscribeEvent
    public static void registerBlockColor(ColorHandlerEvent.Block event) {

        event.getBlockColors().register((blockstate, blockReader, blockPos, color) ->
        {if (color == 1) {return MowLibColorReference.getColorFromStateInt(blockstate);} else {return -1;}}, DeferredRegisterTileBlocks.BLOCK_PEDESTAL.get());
    }*/
}
