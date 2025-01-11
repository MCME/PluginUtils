package com.mcmiddleearth.pluginutil.plotStoring;

public class EntityTypeUtil {

    public static final String PAINTING = "painting";
    public static final String ITEM_FRAME = "item_frame";
    public static final String GLOW_ITEM_FRAME = "glow_item_frame";

    private static final String MINECRAFT_PREFIX = "minecraft:";

    public static boolean isHanging(String type) {
        return type.equals(PAINTING) || type.equals(ITEM_FRAME) || type.equals(GLOW_ITEM_FRAME)
                || type.equals(MINECRAFT_PREFIX+PAINTING)
                || type.equals(MINECRAFT_PREFIX+ITEM_FRAME) || type.equals(MINECRAFT_PREFIX+GLOW_ITEM_FRAME);
    }
    public static boolean isPainting(String type) {
        return type.equals(PAINTING) || type.equals(MINECRAFT_PREFIX+PAINTING);
    }

    public static boolean isItemFrame(String type) {
        return type.equals(ITEM_FRAME) || type.equals(GLOW_ITEM_FRAME)
            || type.equals(MINECRAFT_PREFIX+ITEM_FRAME) || type.equals(MINECRAFT_PREFIX+GLOW_ITEM_FRAME);
    }

}
