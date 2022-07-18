package com.mowmaster.mowlib.MowLibUtils;

import net.minecraft.ChatFormatting;
import net.minecraft.Util;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

import java.util.List;

import static com.mowmaster.mowlib.MowLibUtils.MowLibReferences.MODID;

public class MowLibMessageUtils
{

    public static Component getMowLibComponentLocalized(String localizedMessage, @Nullable ChatFormatting style)
    {
        if(style != null)return Component.translatable(MODID + localizedMessage).withStyle(style);
        return Component.translatable(MODID + localizedMessage);
    }

    
    public static Component makeComponent(String contents, @Nullable ChatFormatting style)
    {
        if(style != null)return Component.translatable(contents).withStyle(style);
        return Component.translatable(contents);
    }

    public static void messagePopup(Player player, ChatFormatting color, String localizedMessage)
    {
        MutableComponent message = Component.translatable(localizedMessage);
        message.withStyle(color);
        player.displayClientMessage(message,true);
    }

    public static void messagePopupText(Player player, ChatFormatting color, String unlocMessage)
    {
        MutableComponent message = Component.literal(unlocMessage);
        message.withStyle(color);
        player.displayClientMessage(message,true);
    }

    public static void messagePopupWithAppend(String MODID, Player player, ChatFormatting color, String localizedMessage, List<String> appendedMessage)
    {
        MutableComponent message = (localizedMessage.contains(MODID))?(Component.translatable(localizedMessage)):(Component.literal(localizedMessage));
        for(int i = 0; i<appendedMessage.size(); i++)
        {
            if(appendedMessage.get(i).contains(MODID))
            {
                message.append(Component.translatable(appendedMessage.get(i)));
            }
            else
            {
                message.append(appendedMessage.get(i));
            }
        }
        message.withStyle(color);
        player.displayClientMessage(message,true);
    }

    public static void messagePlayerChat(Player player, ChatFormatting color, String localizedMessage)
    {
        MutableComponent message = Component.translatable(localizedMessage);
        message.withStyle(color);
        player.displayClientMessage(message,false);
    }

    public static void messagePlayerChatText(Player player, ChatFormatting color, String unlocMessage)
    {
        MutableComponent message = Component.literal(unlocMessage);
        message.withStyle(color);
        player.displayClientMessage(message,false);
    }

    public static void messagePlayerChatWithAppend(String MODID, Player player, ChatFormatting color, String localizedMessage, List<String> appendedMessage)
    {
        MutableComponent message = (localizedMessage.contains(MODID))?(Component.translatable(localizedMessage)):(Component.literal(localizedMessage));
        for(int i = 0; i<appendedMessage.size(); i++)
        {
            if(appendedMessage.get(i).contains(MODID))
            {
                message.append(Component.translatable(appendedMessage.get(i)));
            }
            else
            {
                message.append(appendedMessage.get(i));
            }
        }
        message.withStyle(color);
        player.displayClientMessage(message,false);
    }
}
