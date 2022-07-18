package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.ComponentContents;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.level.block.BedBlock;

import java.util.List;
import java.util.Optional;

import net.minecraft.network.chat.FormattedText.ContentConsumer;
import net.minecraft.network.chat.FormattedText.StyledContentConsumer;

public class MowLibNameComponentUtils
{
    public static Component createComponentName(Component componentStart, String name)
    {
        Component component = new Component() {

            @Override
            public Style getStyle() {
                return componentStart.getStyle();
            }

            @Override
            public ComponentContents getContents() {
                return componentStart.getContents();
            }

            @Override
            public String getString() {
                return name + componentStart.getString();
            }

            @Override
            public String getString(int p_130669_) {
                return name + componentStart.getString(p_130669_);
            }

            @Override
            public List<Component> getSiblings() {
                return componentStart.getSiblings();
            }

            @Override
            public MutableComponent plainCopy() {
                return Component.super.plainCopy();
            }

            @Override
            public MutableComponent copy() {
                return Component.super.copy();
            }

            @Override
            public FormattedCharSequence getVisualOrderText() {
                return componentStart.getVisualOrderText();
            }

            @Override
            public <T> Optional<T> visit(StyledContentConsumer<T> p_130679_, Style p_130680_) {
                return Component.super.visit(p_130679_, p_130680_);
            }

            @Override
            public <T> Optional<T> visit(ContentConsumer<T> p_130677_) {
                return Component.super.visit(p_130677_);
            }

            @Override
            public List<Component> toFlatList(Style p_178406_) {
                return Component.super.toFlatList(p_178406_);
            }
        };

        return component;
    }
}
