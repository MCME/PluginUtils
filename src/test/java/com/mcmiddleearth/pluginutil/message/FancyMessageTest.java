package com.mcmiddleearth.pluginutil.message;

import static com.mcmiddleearth.pluginutil.message.RenderedText.plainText;
import static com.mcmiddleearth.pluginutil.message.RenderedText.styleOf;
import static com.mcmiddleearth.pluginutil.message.RenderedText.tooltipOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockbukkit.mockbukkit.MockBukkit;
import org.mockbukkit.mockbukkit.ServerMock;
import org.mockbukkit.mockbukkit.entity.PlayerMock;

class FancyMessageTest {

    private static final String WEBSITE = "https://www.mcmiddleearth.com";

    private ServerMock server;
    private PlayerMock player;
    private MessageUtil messageUtil;

    @BeforeEach
    void setUp() {
        server = MockBukkit.mock();
        player = server.addPlayer();
        messageUtil = new MessageUtil();
        messageUtil.setPluginName("Test");
    }

    @AfterEach
    void tearDown() {
        MockBukkit.unmock();
    }

    @Test
    void clickablePartPutsItsCommandIntoTheChatInput() {
        new FancyMessage(MessageType.INFO, messageUtil).addClickable("stone", "//set stone").send(player);

        assertEquals(ClickEvent.suggestCommand("//set stone"), styleOf(received(player), "stone").clickEvent());
    }

    @Test
    void clickablePartRunsItsCommandAfterSetRunDirect() {
        new FancyMessage(MessageType.INFO, messageUtil).addClickable("next page", "/list 2").setRunDirect()
                .send(player);

        assertEquals(ClickEvent.runCommand("/list 2"), styleOf(received(player), "next page").clickEvent());
    }

    @Test
    void clickableWebAddressOpensInTheBrowser() {
        new FancyMessage(MessageType.INFO, messageUtil).addClickable("website", WEBSITE).setRunDirect().send(player);

        assertEquals(ClickEvent.openUrl(WEBSITE), styleOf(received(player), "website").clickEvent());
    }

    @Test
    void clickablePartCopiesItsTextAfterSetCopyToClipboardEvenForAWebAddress() {
        new FancyMessage(MessageType.INFO, messageUtil)
                .addClickable("block ", "minecraft:stone")
                .addClickable("website", WEBSITE)
                .setCopyToClipboard()
                .send(player);

        Component message = received(player);
        assertEquals(ClickEvent.copyToClipboard("minecraft:stone"), styleOf(message, "block ").clickEvent());
        assertEquals(ClickEvent.copyToClipboard(WEBSITE), styleOf(message, "website").clickEvent());
    }

    @Test
    void tooltippedPartShowsItsTextInItsColours() {
        new FancyMessage(MessageType.INFO, messageUtil).addTooltipped("hover me", "§6Right §eclick").send(player);

        Component tooltip = tooltipOf(received(player), "hover me");
        assertEquals("Right click", plainText(tooltip));
        assertEquals(NamedTextColor.GOLD, styleOf(tooltip, "Right ").color());
        assertEquals(NamedTextColor.YELLOW, styleOf(tooltip, "click").color());
    }

    @Test
    void fancyPartHasBothClickAndTooltip() {
        new FancyMessage(MessageType.INFO, messageUtil)
                .addFancy("oak_stairs", "//set oak_stairs", "Click to set this block").send(player);

        Component message = received(player);
        assertEquals(ClickEvent.suggestCommand("//set oak_stairs"), styleOf(message, "oak_stairs").clickEvent());
        assertEquals("Click to set this block", plainText(tooltipOf(message, "oak_stairs")));
    }

    @Test
    void messageKeepsItsPrefixColourCodesAndFormats() {
        new FancyMessage(MessageType.INFO, messageUtil).addSimple("§lBold §cred §rplain #00ff00hex").send(player);

        Component message = received(player);
        assertEquals("[Test] Bold red plain hex", plainText(message));
        assertEquals(NamedTextColor.AQUA, styleOf(message, "[Test] ").color());
        Style bold = styleOf(message, "Bold ");
        assertEquals(NamedTextColor.AQUA, bold.color());
        assertEquals(TextDecoration.State.TRUE, bold.decoration(TextDecoration.BOLD));
        Style red = styleOf(message, "red ");
        assertEquals(NamedTextColor.RED, red.color());
        assertNotEquals(TextDecoration.State.TRUE, red.decoration(TextDecoration.BOLD));
        Style plain = styleOf(message, "plain ");
        assertEquals(NamedTextColor.AQUA, plain.color());
        assertEquals(TextDecoration.State.FALSE, plain.decoration(TextDecoration.BOLD));
        assertEquals(TextColor.color(0x00ff00), styleOf(message, "hex").color());
    }

    @Test
    void formatCodesUnderlineStrikeItalicAndObfuscate() {
        new FancyMessage(MessageType.HIGHLIGHT, messageUtil).addSimple("§nunder §mstruck §oitalic §kmagic")
                .send(player);

        Component message = received(player);
        assertEquals(NamedTextColor.GOLD, styleOf(message, "under ").color());
        assertEquals(TextDecoration.State.TRUE, styleOf(message, "under ").decoration(TextDecoration.UNDERLINED));
        assertEquals(TextDecoration.State.TRUE,
                styleOf(message, "struck ").decoration(TextDecoration.STRIKETHROUGH));
        assertEquals(TextDecoration.State.TRUE, styleOf(message, "italic ").decoration(TextDecoration.ITALIC));
        assertEquals(TextDecoration.State.TRUE, styleOf(message, "magic").decoration(TextDecoration.OBFUSCATED));
    }

    /**
     * FancyMessage used to be sent as a JSON array, whose first element is the parent of the others. Without a
     * prefix, the first part is the player's own text, so later parts show its tooltip unless they have their own.
     */
    @Test
    void laterPartsShowTheFirstPartsTooltipWhenThereIsNoPrefix() {
        new FancyMessage(MessageType.WHITE, messageUtil)
                .addFancy("§3/help", "/help ", "Lists the commands")
                .addClickable("§f shows this list", "/help ")
                .send(player);

        Component message = received(player);
        Style description = styleOf(message, " shows this list");
        assertEquals(NamedTextColor.WHITE, description.color());
        assertEquals(ClickEvent.suggestCommand("/help "), description.clickEvent());
        assertEquals("Lists the commands", plainText(tooltipOf(message, " shows this list")));
    }

    @Test
    void broadcastReachesEveryPlayerWithItsClick() {
        PlayerMock other = server.addPlayer();

        MessageUtil.sendBroadcastMessage(new FancyMessage(MessageType.INFO, messageUtil).addClickable("spawn", "/spawn")
                .setRunDirect());

        assertEquals(ClickEvent.runCommand("/spawn"), styleOf(received(player), "spawn").clickEvent());
        assertEquals(ClickEvent.runCommand("/spawn"), styleOf(received(other), "spawn").clickEvent());
    }

    @Test
    void emptyMessageSendsNothing() {
        new FancyMessage(MessageType.INFO_NO_PREFIX, messageUtil).send(player);

        assertNull(player.nextComponentMessage());
    }

    private static Component received(PlayerMock player) {
        Component message = player.nextComponentMessage();
        assertNotNull(message, player.getName() + " got no message");
        return message;
    }
}
