package com.mcmiddleearth.pluginutil.message;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.HoverEvent;
import net.kyori.adventure.text.flattener.ComponentFlattener;
import net.kyori.adventure.text.flattener.FlattenerListener;
import net.kyori.adventure.text.format.Style;

/**
 * Reads a chat component the way the client draws it: each piece of text with its own style, plus whatever it
 * inherits from the components it is nested in.
 */
final class RenderedText {

    private RenderedText() {
    }

    /**
     * The style the client draws a piece of text with. Fails if no piece of the component is exactly that text.
     */
    static Style styleOf(Component component, String text) {
        return stylesOf(component, text).get(0);
    }

    /**
     * The styles the client draws each piece of exactly this text with, in order. Fails if there is none.
     */
    static List<Style> stylesOf(Component component, String text) {
        List<String> pieces = new ArrayList<>();
        List<Style> matches = new ArrayList<>();
        Deque<Style> styles = new ArrayDeque<>();
        styles.push(Style.empty());
        ComponentFlattener.basic().flatten(component, new FlattenerListener() {
            @Override
            public void pushStyle(Style style) {
                styles.push(style.merge(styles.peek(), Style.Merge.Strategy.IF_ABSENT_ON_TARGET));
            }

            @Override
            public void component(String piece) {
                pieces.add(piece);
                if (piece.equals(text)) {
                    matches.add(styles.peek());
                }
            }

            @Override
            public void popStyle(Style style) {
                styles.pop();
            }
        });
        if (matches.isEmpty()) {
            fail("No piece \"" + text + "\" in " + pieces);
        }
        return matches;
    }

    /**
     * The text a player sees when hovering over a piece of text. Fails if that piece has no tooltip.
     */
    static Component tooltipOf(Component component, String text) {
        HoverEvent<?> hover = styleOf(component, text).hoverEvent();
        assertNotNull(hover, "\"" + text + "\" has no tooltip");
        assertEquals(HoverEvent.Action.SHOW_TEXT, hover.action());
        return (Component) hover.value();
    }

    /**
     * All text of a component, without its styles.
     */
    static String plainText(Component component) {
        StringBuilder text = new StringBuilder();
        ComponentFlattener.basic().flatten(component, text::append);
        return text.toString();
    }
}
