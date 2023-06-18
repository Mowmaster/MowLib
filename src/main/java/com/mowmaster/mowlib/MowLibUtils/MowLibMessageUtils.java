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

    public static void messagePopup(Player player, Component componentMessage, @Nullable ChatFormatting style)
    {
        MutableComponent message = componentMessage.copy();
        if(style != null) { message.withStyle(style); }
        player.displayClientMessage(message, true);
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



    public static Component getMowLibComponentLocalizedWithoutStyle(String localizedMessage)
    {
        return Component.translatable(MODID + localizedMessage);
    }


    public static Component makeComponentWithoutStyle(String contents)
    {
        return Component.translatable(contents);
    }

    public static void messagePopupWithoutStyle(Player player, Component componentMessage)
    {
        MutableComponent message = componentMessage.copy();
        player.displayClientMessage(message, true);
    }

    public static void messagePopupWithoutStyle(Player player, String localizedMessage)
    {
        MutableComponent message = Component.translatable(localizedMessage);
        player.displayClientMessage(message,true);
    }

    public static void messagePopupTextWithoutStyle(Player player, String unlocMessage)
    {
        MutableComponent message = Component.literal(unlocMessage);
        player.displayClientMessage(message,true);
    }

    public static void messagePopupWithAppendWithoutStyle(String MODID, Player player, String localizedMessage, List<String> appendedMessage)
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
        player.displayClientMessage(message,true);
    }

    public static void messagePlayerChatWithoutStyle(Player player, String localizedMessage)
    {
        MutableComponent message = Component.translatable(localizedMessage);
        player.displayClientMessage(message,false);
    }

    public static void messagePlayerChatTextWithoutStyle(Player player, String unlocMessage)
    {
        MutableComponent message = Component.literal(unlocMessage);
        player.displayClientMessage(message,false);
    }

    public static void messagePlayerChatWithAppendWithoutStyle(String MODID, Player player, String localizedMessage, List<String> appendedMessage)
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
        player.displayClientMessage(message,false);
    }

    public static void modeBasedTextOutputPopup(Player player, int mode, boolean localized, String modid, List<String> modeTextList, List<ChatFormatting> modeColorList)
    {
        modeTextList.add(".error");
        modeColorList.add(ChatFormatting.DARK_RED);
        int getMode = (mode>=modeTextList.size())?(modeTextList.size()-1):(mode);
        MutableComponent type;
        if(localized) { type = Component.translatable(modid + modeTextList.get(getMode)); }
        else { type = Component.literal(modeTextList.get(getMode)); }
        type.withStyle(modeColorList.get(getMode));
        player.displayClientMessage(type, true);
    }


}
