/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.message;

import com.google.gson.JsonObject;
import com.mcmiddleearth.pluginutil.message.config.FancyMessageConfigUtil;
import java.util.ArrayList;
import java.util.List;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * This class provides an easy to use way to send clickable and tooltipped text chat messages to players.
 * When a player hovers with mouse cursor over a tooltipped message he will see the tooltip text.
 * When he clicks at a clickable message he will get the associated text in chat.
 * @author Eriol_Eandur
 */
public final class FancyMessage {

    private final List<String[]> data = new ArrayList<>();

    private boolean runDirect = false;
    
    private ChatColor baseColor;
    
    private MessageUtil messageUtil;

    /**
     * Create a new fancy info message.
     * @param messageUtil MessageUtil object to handle sending of the message.
     */
    public FancyMessage(MessageUtil messageUtil) {
        this(MessageType.INFO, messageUtil);
    }
    
    /**
     * Create a new fancy message of any type.
     *
     * @param messageType Type of the message (default color and prefix)
     * @param messageUtil MessageUtil object to handle sending of the message.
     */
    public FancyMessage(MessageType messageType, MessageUtil messageUtil) {
        baseColor = messageType.getBaseColor();
        this.messageUtil = messageUtil;
        String prefix = "";
        switch(messageType) {
            case INFO:
            case ERROR:
            case HIGHLIGHT:
                prefix = messageUtil.getPREFIX();
                break;
            case INFO_INDENTED:
            case ERROR_INDENTED:
            case HIGHLIGHT_INDENTED:
                prefix = messageUtil.getNOPREFIX();
        }
        addSimple(prefix);
    }
    
    /**
     * Create a new fancy message of any type and base color.
     *
     * @param messageType Type of the message (default color and prefix)
     * @param messageUtil MessageUtil object to handle sending of the message.
     * @param baseColor Defaut color to use.
     */
    public FancyMessage(MessageType messageType, MessageUtil messageUtil, ChatColor baseColor) {
        this(messageType, messageUtil);
        this.baseColor = baseColor;
    }

    public ChatColor getBaseColor() {
        return baseColor;
    }

    public MessageUtil getMessageUtil() {
        return messageUtil;
    }

    /**
     * Append a simple text to the message which is not tooltipped and not clickable.
     * @param text Text to append to the message
     * @return Message with new text appended
     */
    public FancyMessage addSimple(String text){
        //data.add(new String[]{text,null,null});
        return addFancy(text,null,null);
        //return this;
    }

    /**
     * Append a clickable text to the message.
     * @param text Text to append to the message
     * @param onClickCommand Text to put into player chat when he clicks the text
     * @return Message with new text appended
     */
    public FancyMessage addClickable(String text, String onClickCommand) {
        //data.add(new String[]{text,onClickCommand,null});
        return addFancy(text, onClickCommand, null);
        //return this;
    }

    /**
     * Append a clickable text to the message.
     * @param text Text to append to the message
     * @param onHoverText Text to disply when a player hovers the mouse cursor over the text
     * @return Message with new text appended
     */
    public FancyMessage addTooltipped(String text, String onHoverText) {
        //data.add(new String[]{text,null,onHoverText});
        return addFancy(text, null, onHoverText);
        //return this;
    }

    /**
     * Append a clickable and tooltipped text to the message.
     * @param text Text to append to the message
     * @param onClickCommand Text to put into player chat when he clicks the text
     * @param onHoverText Text to disply when a player hovers the mouse cursor over the text
     * @return Message with new text appended
     */
    public FancyMessage addFancy(String text, String onClickCommand, String onHoverText) {
        //JsonMessageParser.Format format = new JsonMessageParser.Format();
        String color = colorString(baseColor);
        while(text.length()>0) {
            String format = "";
            int colorPos = text.indexOf("§");
            int hexColorPos = text.indexOf("#");
            ChatColor chatColor = null;
            if(colorPos != 0 && hexColorPos != 0) {
                chatColor = baseColor;
            } else if(colorPos == 0){
                chatColor = chatColor(text.charAt(1));
                text = text.substring(2);
            } else {
                color = text.substring(0,7);
                text = text.substring(7);
            }
            if(chatColor != null && chatColor.isColor()) {
                color = colorString(chatColor);
            } else if(chatColor != null && chatColor.isFormat()) {
                switch (chatColor) {
                    case BOLD -> format = ", \"bold\" : true";
                    case UNDERLINE -> format  = ", \"underlined\" : true";
                    case STRIKETHROUGH -> format  = ", \"strikethrough\" : true";
                    case MAGIC -> format  = ", \"obfuscated\" : true";
                    case ITALIC -> format  = ", \"italic\" : true";
                }
            } else if(chatColor != null) {
                format = ", \"bold\" : false, \"underlined\" : false, \"strikethrough\" : false, \"obfuscated\" : false, \"italic\" : false";
                color = colorString(baseColor);
            }
            colorPos = text.indexOf("§");
            hexColorPos = text.indexOf("#");
            String textPart;
            if(colorPos < 0 && hexColorPos < 0) {
                textPart = text;
                text = "";
            } else if(colorPos < 0 || (hexColorPos >= 0 && hexColorPos < colorPos)) {
                textPart = text.substring(0, hexColorPos);
                text = text.substring(hexColorPos);
            } else{
                textPart = text.substring(0, colorPos);
                text = text.substring(colorPos);
            }
            data.add(new String[]{textPart, onClickCommand, onHoverText, color, format});
        }
        return this;
    }
    
    /**
     * Defines a new default color which will be used for each new line.
     * @param color new default color
     * @return same message
     */
    public FancyMessage setBaseColor(ChatColor color) {
        baseColor = color;
        return this;
    }
    
    /**
     * Clicking at the message will excecute the associated text as a command instead of
     * puting it in text chat.
     * @return same message
     */
    public FancyMessage setRunDirect() {
        this.runDirect = true;
        return this;
    }

    /**
     * Send a fancy message to a player.
     * @param recipient Player who will get the message.
     * @return same message
     */
    public FancyMessage send(Player recipient) {
        String rawText = "[";
        String action;
        if(runDirect) {
            action = "run_command";
        } else {
            action = "suggest_command";
        }
        boolean first = true;
        for(String[] messageData: data) {
            String message = messageData[0];
            String command = messageData[1];
            String hoverText = messageData[2];
            String color = (messageData.length>3?messageData[3]:colorString(baseColor));
            String format = (messageData.length>4?messageData[4]:"");
            message = replaceQuotationMarks(message);
            if(first) {
                first = false; 
            }
            else {
                rawText = rawText.concat(",");
            }
            rawText = rawText.concat("{\"text\":\""+message+"\",\"color\":\""+color+"\""+format);
            if(command!=null) {
                String thisAction = action;
                if(command.startsWith("http")) {
                    thisAction = "open_url";
                }
                command = replaceQuotationMarks(command);
                rawText = rawText.concat(",\"clickEvent\":{\"action\":\""+thisAction+"\",\"value\":\"");
                rawText = rawText.concat(command+"\"}");
            }
            if(hoverText!=null) {
                hoverText = replaceQuotationMarks(hoverText);
                rawText = rawText.concat(",\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[");
                rawText = rawText.concat(JsonMessageParser.parseColoredText(hoverText)+"]}");
            }
            rawText = rawText.concat("}");
        }
        rawText = rawText.concat("]");
        MessageUtil.sendRawMessage(recipient, rawText);
        return this;
    }
    
    /**
     * Store the fancy message in a configuration.
     * @param config where to store the message
     */
    public void saveToConfig(ConfigurationSection config) {
        FancyMessageConfigUtil.store(data, config);
    }
    
    private String replaceQuotationMarks(String string) {
        return string.replaceAll("\"", "__stRe__\"").replaceAll("__stRe__", "\\\\");
    }
    
    public static String colorString(ChatColor color) {
        return switch (color) {
            case BLACK -> "black";
            case DARK_BLUE -> "dark_blue";
            case DARK_GREEN -> "dark_green";
            case DARK_AQUA -> "dark_aqua";
            case DARK_RED -> "dark_red";
            case DARK_PURPLE -> "dark_purple";
            case GOLD -> "gold";
            case GRAY -> "gray";
            case DARK_GRAY -> "dark_gray";
            case BLUE -> "blue";
            case GREEN -> "green";
            case AQUA -> "aqua";
            case RED -> "red";
            case LIGHT_PURPLE -> "light_purple";
            case YELLOW -> "yellow";
            case WHITE -> "white";
            case BOLD -> "bold";
            case UNDERLINE -> "underline";
            case ITALIC -> "italic";
            case STRIKETHROUGH -> "strikethrough";
            case MAGIC -> "obfuscated";
            default -> "reset";
        };
    }

    public static ChatColor chatColor(char colorCode) {
        return switch(colorCode) {
            case '0' -> ChatColor.BLACK;
            case '1' -> ChatColor.DARK_BLUE;
            case '2' -> ChatColor.DARK_GREEN;
            case '3' -> ChatColor.DARK_AQUA;
            case '4' -> ChatColor.DARK_RED;
            case '5' -> ChatColor.DARK_PURPLE;
            case '6' -> ChatColor.GOLD;
            case '7' -> ChatColor.GRAY;
            case '8' -> ChatColor.DARK_GRAY;
            case '9' -> ChatColor.BLUE;
            case 'a' -> ChatColor.GREEN;
            case 'b' -> ChatColor.AQUA;
            case 'c' -> ChatColor.RED;
            case 'd' -> ChatColor.LIGHT_PURPLE;
            case 'e' -> ChatColor.YELLOW;
            case 'f' -> ChatColor.WHITE;
            case 'l' -> ChatColor.BOLD;
            case 'n' -> ChatColor.UNDERLINE;
            case 'o' -> ChatColor.ITALIC;
            case 'm' -> ChatColor.STRIKETHROUGH;
            case 'k' -> ChatColor.MAGIC;
            default -> ChatColor.RESET;
        };
    }

    public List<String[]> getData() {
        return data;
    }

    public boolean isRunDirect() {
        return runDirect;
    }

    public JsonObject parseJson() {
        return JsonMessageParser.parse(this);

    }
}