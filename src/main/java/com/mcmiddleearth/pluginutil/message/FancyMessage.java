/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.mcmiddleearth.pluginutil.message;

import com.google.gson.JsonObject;
import com.mcmiddleearth.pluginutil.message.config.FancyMessageConfigUtil;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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

    private boolean copyToClipboard = false;

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
            int colorPos = colorCodeIndex(text);
            int hexColorPos = hexColorIndex(text);
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
            colorPos = colorCodeIndex(text);
            hexColorPos = hexColorIndex(text);
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
     * Where the first § is that has a character after it, or -1: a § at the very end is plain text.
     */
    private static int colorCodeIndex(String text) {
        int index = text.indexOf("§");
        return (index < text.length()-1?index:-1);
    }

    /**
     * Where the first # followed by six hex digits is, or -1: any other # is plain text.
     */
    private static int hexColorIndex(String text) {
        int index = text.indexOf("#");
        while(index >= 0 && !JsonMessageParser.isHexColor(text, index+1)) {
            index = text.indexOf("#", index+1);
        }
        return index;
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
     * Clicking at the message will copy the associated text to the player's clipboard
     * instead of running or suggesting a command.
     * @return same message
     */
    public FancyMessage setCopyToClipboard() {
        this.copyToClipboard = true;
        return this;
    }

    /**
     * Send a fancy message to a player.
     * @param recipient Player who will get the message.
     * @return same message
     */
    public FancyMessage send(Player recipient) {
        TextComponent.Builder message = null;
        for(String[] messageData: data) {
            TextComponent part = toComponent(messageData);
            // Like the JSON array this message used to be sent as, the first part is the parent of the others:
            // they inherit its style wherever they set none of their own.
            if(message == null) {
                message = part.toBuilder();
            } else {
                message.append(part);
            }
        }
        if(message != null) {
            recipient.sendMessage(message.build());
        }
        return this;
    }
    
    /**
     * Store the fancy message in a configuration.
     * @param config where to store the message
     */
    public void saveToConfig(ConfigurationSection config) {
        FancyMessageConfigUtil.store(data, config);
    }
    
    private TextComponent toComponent(String[] messageData) {
        String command = messageData[1];
        String hoverText = messageData[2];
        // A typed \n is a line break, as it was when the text went to /tellraw inside a JSON string.
        TextComponent.Builder part = Component.text().content(messageData[0].replace("\\n", "\n"));
        TextColor color = textColor(messageData.length>3?messageData[3]:colorString(baseColor));
        if(color != null) {
            part.color(color);
        }
        if(messageData.length>4) {
            addFormat(part, messageData[4]);
        }
        if(command!=null) {
            part.clickEvent(clickEvent(command));
        }
        if(hoverText!=null && !hoverText.isEmpty()) {
            part.hoverEvent(HoverEvent.showText(tooltip(hoverText)));
        }
        return part.build();
    }

    private static Component tooltip(String hoverText) {
        try {
            return GsonComponentSerializer.gson().deserializeFromTree(JsonMessageParser.parseColoredText(hoverText));
        } catch(RuntimeException ex) {
            Logger.getLogger(FancyMessage.class.getName()).log(Level.WARNING, "Could not read the codes in tooltip \""
                    + hoverText + "\" (" + ex + "), so only its § codes are shown.");
            return LegacyComponentSerializer.legacySection().deserialize(hoverText);
        }
    }

    /**
     * The click event for a part. A web address Minecraft cannot open is copied instead, so that the part still has
     * a click of its own rather than the first part's.
     */
    private ClickEvent<?> clickEvent(String command) {
        if(copyToClipboard) {
            return ClickEvent.copyToClipboard(command);
        } else if(command.startsWith("http")) {
            if(isWebAddress(command)) {
                return ClickEvent.openUrl(command);
            }
            Logger.getLogger(FancyMessage.class.getName()).log(Level.WARNING, "Minecraft cannot open \"" + command
                    + "\", so clicking it in a message copies it instead.");
            return ClickEvent.copyToClipboard(command);
        } else if(runDirect) {
            return ClickEvent.runCommand(allowedInChat(command));
        } else {
            return ClickEvent.suggestCommand(allowedInChat(command));
        }
    }

    /**
     * Whether this web address can be sent and opened. Adventure takes it only if it is a URI; Paper sends it as a
     * URI with https:// in front when it has no "://"; and a player's client can only read http and https addresses.
     */
    static boolean isWebAddress(String url) {
        try {
            new URI(url);
            String scheme = new URI(url.contains("://")?url:"https://"+url).getScheme();
            return scheme != null && (scheme.equalsIgnoreCase("http") || scheme.equalsIgnoreCase("https"));
        } catch(URISyntaxException ex) {
            return false;
        }
    }

    /**
     * The command without the characters Minecraft does not allow in chat: §, control characters and DEL. Paper
     * cannot send a click event holding one of them and would drop the whole message.
     */
    static String allowedInChat(String command) {
        StringBuilder allowed = new StringBuilder(command.length());
        for(char c: command.toCharArray()) {
            if(c != '§' && c >= ' ' && c != 127) {
                allowed.append(c);
            }
        }
        return allowed.toString();
    }

    /**
     * A colour as addFancy stores it: a name from colorString, such as "dark_aqua", or a hex colour such as
     * "#00ff00". Anything else, such as "reset", is no colour.
     */
    private static TextColor textColor(String color) {
        if(color == null) {
            return null;
        }
        return color.startsWith("#") ? TextColor.fromHexString(color) : NamedTextColor.NAMES.value(color);
    }

    /**
     * Applies a format as addFancy stores it: JSON members such as ', "bold" : true'.
     */
    private static void addFormat(TextComponent.Builder part, String format) {
        if(format == null) {
            return;
        }
        for(TextDecoration decoration: TextDecoration.values()) {
            String member = "\"" + TextDecoration.NAMES.key(decoration) + "\" : ";
            if(format.contains(member + "true")) {
                part.decoration(decoration, true);
            } else if(format.contains(member + "false")) {
                part.decoration(decoration, false);
            }
        }
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

    public boolean isCopyToClipboard() {
        return copyToClipboard;
    }

    public JsonObject parseJson() {
        return JsonMessageParser.parse(this);

    }
}