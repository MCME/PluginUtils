package com.mcmiddleearth.pluginutil.message;

import static com.mcmiddleearth.pluginutil.message.RenderedText.plainText;
import static com.mcmiddleearth.pluginutil.message.RenderedText.styleOf;
import static com.mcmiddleearth.pluginutil.message.RenderedText.tooltipOf;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class MessageUtilTest {

    private static final String WEBSITE = "https://www.mcmiddleearth.com";

    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        player = server.addPlayer();
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    /**
     * The form FancyMessage itself sent before, with the field names Minecraft used before 1.21.5.
     */
    @Test
    void rawMessageWithTheOldFieldNamesKeepsItsClickAndTooltip() {
        MessageUtil.sendRawMessage(player, "[{\"text\":\"[Test] \",\"color\":\"aqua\"},"
                + "{\"text\":\"stone\",\"color\":\"dark_aqua\","
                + "\"clickEvent\":{\"action\":\"suggest_command\",\"value\":\"//set stone\"},"
                + "\"hoverEvent\":{\"action\":\"show_text\",\"contents\":[{\"text\":\"Click me\"}]}}]");

        Component message = received();
        assertEquals("[Test] stone", plainText(message));
        assertEquals(NamedTextColor.DARK_AQUA, styleOf(message, "stone").color());
        assertEquals(ClickEvent.suggestCommand("//set stone"), styleOf(message, "stone").clickEvent());
        assertEquals("Click me", plainText(tooltipOf(message, "stone")));
    }

    @Test
    void rawMessageWithTheOldFieldNamesRunsOpensAndCopies() {
        MessageUtil.sendRawMessage(player, "[\"\","
                + "{\"text\":\"spawn\",\"clickEvent\":{\"action\":\"run_command\",\"value\":\"/spawn\"}},"
                + "{\"text\":\"website\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"" + WEBSITE + "\"}},"
                + "{\"text\":\"block\","
                + "\"clickEvent\":{\"action\":\"copy_to_clipboard\",\"value\":\"minecraft:stone\"}}]");

        Component message = received();
        assertEquals(ClickEvent.runCommand("/spawn"), styleOf(message, "spawn").clickEvent());
        assertEquals(ClickEvent.openUrl(WEBSITE), styleOf(message, "website").clickEvent());
        assertEquals(ClickEvent.copyToClipboard("minecraft:stone"), styleOf(message, "block").clickEvent());
    }

    /**
     * Before Minecraft 1.16, a show_text tooltip held its text in "value" rather than "contents".
     */
    @Test
    void rawMessageWithATooltipInValueShowsIt() {
        MessageUtil.sendRawMessage(player,
                "{\"text\":\"info\",\"hoverEvent\":{\"action\":\"show_text\",\"value\":\"Built in 2016\"}}");

        assertEquals("Built in 2016", plainText(tooltipOf(received(), "info")));
    }

    @Test
    void rawMessageWithTheCurrentFieldNamesWorks() {
        MessageUtil.sendRawMessage(player, "{\"text\":\"spawn\","
                + "\"click_event\":{\"action\":\"run_command\",\"command\":\"/spawn\"},"
                + "\"hover_event\":{\"action\":\"show_text\",\"value\":\"Back to spawn\"}}");

        Component message = received();
        assertEquals(ClickEvent.runCommand("/spawn"), styleOf(message, "spawn").clickEvent());
        assertEquals("Back to spawn", plainText(tooltipOf(message, "spawn")));
    }

    @Test
    void invalidJsonSendsNothingAndThrowsNothing() {
        assertDoesNotThrow(() -> MessageUtil.sendRawMessage(player, "{\"text\":\"broken\""));

        assertNull(player.nextComponentMessage());
    }

    /**
     * A player's client disconnects on an address to open that is neither http nor https, and Adventure refuses one
     * that is no URI.
     */
    static Stream<Arguments> addressesMinecraftCannotOpen() {
        return Stream.of(
                arguments("{\"text\":\"server\","
                        + "\"clickEvent\":{\"action\":\"open_url\",\"value\":\"ts3server://ts.mcmiddleearth.com\"}}",
                        "ts3server://ts.mcmiddleearth.com"),
                arguments("{\"text\":\"server\","
                        + "\"click_event\":{\"action\":\"open_url\",\"url\":\"ftp://files.mcmiddleearth.com\"}}",
                        "ftp://files.mcmiddleearth.com"),
                arguments("{\"text\":\"\",\"extra\":[{\"text\":\"server\","
                        + "\"click_event\":{\"action\":\"open_url\",\"url\":\"" + WEBSITE + "/a page\"}}]}",
                        WEBSITE + "/a page"));
    }

    @ParameterizedTest
    @MethodSource("addressesMinecraftCannotOpen")
    void addressMinecraftCannotOpenArrivesAsACopyClick(String json, String address) {
        MessageUtil.sendRawMessage(player, json);

        Component message = received();
        assertEquals("server", plainText(message));
        assertEquals(ClickEvent.copyToClipboard(address), styleOf(message, "server").clickEvent());
    }

    @Test
    void addressMinecraftCannotOpenInsideATooltipIsCopiedToo() {
        MessageUtil.sendRawMessage(player, "{\"text\":\"info\",\"hoverEvent\":{\"action\":\"show_text\","
                + "\"contents\":{\"text\":\"files\",\"clickEvent\":{\"action\":\"open_url\",\"value\":\"ftp://x\"}}}}");

        Component tooltip = tooltipOf(received(), "info");
        assertEquals(ClickEvent.copyToClipboard("ftp://x"), styleOf(tooltip, "files").clickEvent());
    }

    private Component received() {
        Component message = player.nextComponentMessage();
        assertNotNull(message, "the player got no message");
        return message;
    }
}
