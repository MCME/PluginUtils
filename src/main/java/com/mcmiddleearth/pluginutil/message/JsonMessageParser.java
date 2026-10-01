package com.mcmiddleearth.pluginutil.message;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonMessageParser {

    private static final String CODES = "0123456789abcdefklmnor";

    private static final String HEX_DIGITS = "0123456789abcdefABCDEF";

    public static JsonObject parse(FancyMessage message) {
        JsonObject result = new JsonObject();
        result.addProperty("color",message.getBaseColor()+"");
        //result.add("text", new JsonPrimitive(""));
        JsonArray extra = new JsonArray();
        result.add("extra", extra);
        for(String[] data: message.getData()) {
            JsonObject part = new JsonObject();
            extra.add(part);
                part.add("extra",parseColoredText(data[0]));

                JsonObject hover = new JsonObject();
                part.add("hoverEvent",hover);
                    JsonObject showText = new JsonObject();
                    hover.add("showText",showText);
                        showText.add("extra",parseColoredText(data[2]));

                JsonObject click = new JsonObject();
                part.add("clickEvent",click);
                    click.addProperty("action", (message.isCopyToClipboard()?"copy_to_clipboard":
                                                 (data[1].startsWith("http")?"open_url":
                                                                 (message.isRunDirect()?"run_command":"suggest_command"))));
                    click.addProperty("value",data[1]);
        }
        return result;
    }

    public static void main(String[] args) {
        JsonObject obj = parseColoredText("#ff0099Hey &c\\&Du#00aaff&l");
        //obj = parseColoredText("#ff0099Hey");
        System.out.println(obj.toString());
    }

    /**
     * Turns text with colour and format codes into a JSON text component. A code is {@code §} or {@code &} followed
     * by 0-9 or a-f (a colour, which also ends the formats before it), k-o (a format) or r (white, without formats),
     * in either case; {@code #} followed by six hex digits is a colour too. {@code \#} and {@code \&} stand for a
     * plain {@code #} and {@code &}. Everything else is plain text, so Minecraft can read the result whatever the text.
     * @param text text with colour and format codes
     * @return the text as a JSON text component
     */
    public static JsonObject parseColoredText(String text) {
        JsonObject result = new JsonObject();
        JsonObject current = result;
        Format status = new Format();
        StringBuilder partText = new StringBuilder();
        int i = 0;
        while(i < text.length()) {
            char c = text.charAt(i);
            char next = (i+1 < text.length()?text.charAt(i+1):' ');
            if(c == '\\' && (next == '#' || next == '&' || next == '§')) {
                partText.append(next);
                i += 2;
            } else if((c == '&' || c == '§') && CODES.indexOf(Character.toLowerCase(next)) >= 0) {
                current = nextPart(current, partText);
                char formattingCode = Character.toLowerCase(next);
                switch(formattingCode) {
                    case 'k': status.obfuscated = true; current.addProperty("obfuscated",true); break;
                    case 'l': status.bold = true; current.addProperty("bold",true); break;
                    case 'm': status.strikethrough = true; current.addProperty("strikethrough",true); break;
                    case 'n': status.underline = true; current.addProperty("underlined",true); break;
                    case 'o': status.italic = true; current.addProperty("italic",true); break;
                    case 'r':
                    case 'f': setColor(current, status,"white"); break;
                    case '0': setColor(current, status,"black"); break;
                    case '1': setColor(current, status,"dark_blue"); break;
                    case '2': setColor(current, status,"dark_green"); break;
                    case '3': setColor(current, status,"dark_aqua"); break;
                    case '4': setColor(current, status,"dark_red"); break;
                    case '5': setColor(current, status,"dark_purple"); break;
                    case '6': setColor(current, status,"gold"); break;
                    case '7': setColor(current, status,"gray"); break;
                    case '8': setColor(current, status,"dark_gray"); break;
                    case '9': setColor(current, status,"blue"); break;
                    case 'a': setColor(current, status,"green"); break;
                    case 'b': setColor(current, status,"aqua"); break;
                    case 'c': setColor(current, status,"red"); break;
                    case 'd': setColor(current, status,"light_purple"); break;
                    case 'e': setColor(current, status,"yellow"); break;
                }
                i += 2;
            } else if(c == '#' && isHexColor(text, i+1)) {
                current = nextPart(current, partText);
                current.addProperty("color", text.substring(i, i+7));
                i += 7;
            } else {
                partText.append(c);
                i++;
            }
        }
        current.addProperty("text", partText.toString());
        return result;
    }

    /**
     * Whether the six characters from start on are hex digits, as in a colour like #00ff00.
     */
    static boolean isHexColor(String text, int start) {
        if(start < 0 || start+6 > text.length()) {
            return false;
        }
        for(int i = start; i < start+6; i++) {
            if(HEX_DIGITS.indexOf(text.charAt(i)) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Ends the current part with the text collected for it and starts the next part inside it, so that the next
     * part inherits its style.
     */
    private static JsonObject nextPart(JsonObject current, StringBuilder partText) {
        current.addProperty("text", partText.toString());
        partText.setLength(0);
        JsonObject next = new JsonObject();
        JsonArray extra = new JsonArray();
        extra.add(next);
        current.add("extra",extra);
        return next;
    }

    private static void setColor(JsonObject current, Format status, String color) {
        if(status.obfuscated) current.addProperty("obfuscated",false);
        if(status.bold) current.addProperty("bold",false);
        if(status.strikethrough) current.addProperty("strikethrough",false);
        if(status.underline) current.addProperty("underlined",false);
        if(status.italic) current.addProperty("italic",false);
        current.addProperty("color",color);
    }

    private static class Format {
        public boolean obfuscated;
        public boolean bold;
        public boolean strikethrough;
        public boolean underline;
        public boolean italic;
    }
}
