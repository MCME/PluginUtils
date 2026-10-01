package com.mcmiddleearth.pluginutil.message;

import static com.mcmiddleearth.pluginutil.message.RenderedText.plainText;
import static com.mcmiddleearth.pluginutil.message.RenderedText.styleOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class JsonMessageParserTest {

    static Stream<Arguments> texts() {
        return Stream.of(
                arguments("", ""),
                arguments("#", "#"),
                arguments("&", "&"),
                arguments("§", "§"),
                arguments("Plot #12", "Plot #12"),
                arguments("Rank #1 in the list", "Rank #1 in the list"),
                arguments("/vv stencil [directory] [#page]: Views stencils.",
                        "/vv stencil [directory] [#page]: Views stencils."),
                arguments("Tom & Jerry", "Tom & Jerry"),
                arguments("fish & chips&", "fish & chips&"),
                arguments("#ff0000red", "red"),
                arguments("&cred &lbold", "red bold"),
                arguments("§5dark purple", "dark purple"),
                arguments("&Cupper case code", "upper case code"),
                arguments("\\#ff0000 stays text", "#ff0000 stays text"),
                arguments("fish \\& chips", "fish & chips"));
    }

    /**
     * Whatever the text, the result is a text component that Adventure, like Minecraft, can read.
     */
    @ParameterizedTest
    @MethodSource("texts")
    void coloredTextAlwaysMakesAComponentThatCanBeRead(String text, String shown) {
        assertEquals(shown, plainText(read(text)));
    }

    @Test
    void codesColourAndFormatTheTextAfterThem() {
        Component component = read("&5purple &nunderlined &cred #00ff00hex");

        assertEquals(NamedTextColor.DARK_PURPLE, styleOf(component, "purple ").color());
        Style underlined = styleOf(component, "underlined ");
        assertEquals(NamedTextColor.DARK_PURPLE, underlined.color());
        assertEquals(TextDecoration.State.TRUE, underlined.decoration(TextDecoration.UNDERLINED));
        Style red = styleOf(component, "red ");
        assertEquals(NamedTextColor.RED, red.color());
        assertEquals(TextDecoration.State.FALSE, red.decoration(TextDecoration.UNDERLINED));
        assertEquals(TextColor.color(0x00ff00), styleOf(component, "hex").color());
    }

    private static Component read(String text) {
        return GsonComponentSerializer.gson().deserializeFromTree(JsonMessageParser.parseColoredText(text));
    }
}
