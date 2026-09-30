package com.mcmiddleearth.pluginutil;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import net.kyori.adventure.title.Title;
import net.kyori.adventure.title.TitlePart;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class TitleUtilTest {

    private final List<String> shown = new ArrayList<>();
    private PlayerMock player;

    @BeforeEach
    void setUp() {
        ServerMock server = MockBukkit.mock();
        // A player whose Player.sendTitle fails, so that TitleUtil has to fall back, and who records the title
        // parts sent to it: MockBukkit itself drops Adventure titles.
        player = new PlayerMock(server, "Builder") {
            @Override
            public void sendTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
                throw new IllegalStateException("titles are not available");
            }

            @Override
            public <T> void sendTitlePart(TitlePart<T> part, T value) {
                shown.add(describe(part, value));
            }
        };
        server.addPlayer(player);
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void titleStillShowsWhenPlayerSendTitleFails() {
        TitleUtil.showTitle(player, "Welcome", "to Middle-earth");

        assertEquals(List.of("times 20 80 20", "title Welcome", "subtitle to Middle-earth"), shown);
    }

    @Test
    void fallbackShowsATitleWithQuotationMarksAsTyped() {
        TitleUtil.showTitle(player, "\"Mellon\"", null, 10, 40, 10);

        assertEquals(List.of("times 10 40 10", "title \"Mellon\""), shown);
    }

    private static String describe(TitlePart<?> part, Object value) {
        if (part == TitlePart.TIMES) {
            Title.Times times = (Title.Times) value;
            return "times " + ticks(times.fadeIn()) + " " + ticks(times.stay()) + " " + ticks(times.fadeOut());
        }
        String text = PlainTextComponentSerializer.plainText().serialize((Component) value);
        return (part == TitlePart.TITLE ? "title " : "subtitle ") + text;
    }

    private static long ticks(Duration duration) {
        return duration.toMillis() / 50;
    }
}
