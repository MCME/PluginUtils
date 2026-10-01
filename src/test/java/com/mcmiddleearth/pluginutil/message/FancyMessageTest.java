package com.mcmiddleearth.pluginutil.message;

import static com.mcmiddleearth.pluginutil.message.RenderedText.plainText;
import static com.mcmiddleearth.pluginutil.message.RenderedText.styleOf;
import static com.mcmiddleearth.pluginutil.message.RenderedText.tooltipOf;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.stream.Stream;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextColor;
import net.kyori.adventure.text.format.TextDecoration;
import org.bukkit.ChatColor;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;
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

    /**
     * Usage texts of MCME-Architect commands. /architect help shows each as the tooltip of its command, formatted by
     * hoverFormat.
     */
    static Stream<Arguments> architectUsages() {
        return Stream.of(
                arguments("architect", " help | world | dev | version | reload [#page]: Argument 'help' shows"
                        + " information about Architect commands. 'world' shows a list of all server worlds. 'dev'"
                        + " switches on/off debug messages. 'version' displays Architect version. 'reload' reloads"
                        + " Architect plugin."),
                arguments("redo", " [#n]: Redo up to #n previously undone edits."),
                arguments("undo", " [#n]: Undo up to #n previous edits."),
                arguments("sign", " <#line>: Edits a line of a sign. You need to right-click the sign with a stick"
                        + " first."),
                arguments("sch", " [#page]: Lists all WE schematics, you can click at folder names to navigate into"
                        + " them."),
                arguments("weselect", ": Set command before left-clicking block info in chat. # can work as"
                        + " placeholder for the info."));
    }

    @ParameterizedTest
    @MethodSource("architectUsages")
    void helpLineShowsItsUsageWithHashesAsTooltip(String command, String usage) {
        String tooltip = messageUtil.hoverFormat("/" + command + usage, ":", true);
        new FancyMessage(MessageType.WHITE, messageUtil)
                .addFancy(ChatColor.DARK_AQUA + "/" + command, "/" + command + " ", tooltip)
                .addClickable(ChatColor.WHITE + " short help", "/" + command + " ")
                .send(player);

        assertEquals(withoutCodes(tooltip), plainText(tooltipOf(received(player), "/" + command)));
    }

    /**
     * A line as MCME-Architect builds it from a command's help array (/vv, /noPhy, /get).
     */
    @Test
    void helpArrayLineKeepsTheHashesInItsTextAndTooltip() {
        String[] line = {"/vv stencil ", "[directory] [#page]", ": Views stencils."};
        String tooltip = messageUtil.hoverFormat(line[0] + line[1] + ": " + line[2].substring(2) + " \n "
                + ChatColor.WHITE + "Click to use.", ": ", true);
        new FancyMessage(MessageType.WHITE, messageUtil)
                .addFancy(ChatColor.DARK_AQUA + line[0] + line[1] + ChatColor.WHITE + line[2], line[0], tooltip)
                .send(player);

        Component message = received(player);
        assertEquals("/vv stencil [directory] [#page]: Views stencils.", plainText(message));
        assertEquals(withoutCodes(tooltip), plainText(tooltipOf(message, ": Views stencils.")));
    }

    @Test
    void helpPageDeliversEveryLineAndItsPageLink() {
        List<FancyMessage> lines = new ArrayList<>();
        for (int i = 1; i <= 12; i++) {
            lines.add(new FancyMessage(MessageType.WHITE, messageUtil).addFancy("§3/cmd" + i, "/cmd" + i + " ",
                    messageUtil.hoverFormat("/cmd" + i + " [#page]: Shows page #page.", ":", true)));
        }

        messageUtil.sendFancyListMessage(player, new FancyMessage(MessageType.INFO, messageUtil).addSimple("Help"),
                lines, "/test help", 1);

        assertEquals("[Test] Help", plainText(received(player)));
        for (int i = 1; i <= 10; i++) {
            assertEquals("/cmd" + i, plainText(received(player)));
        }
        Component pager = received(player);
        assertEquals("Page 1/2 [Next ›]", plainText(pager));
        assertEquals(ClickEvent.runCommand("/test help 2"), styleOf(pager, "[Next ›]").clickEvent());
        assertNull(player.nextComponentMessage());
    }

    @Test
    void tooltipInDarkPurpleShows() {
        new FancyMessage(MessageType.INFO, messageUtil).addTooltipped("rare", "§5Mithril").send(player);

        assertEquals(NamedTextColor.DARK_PURPLE, styleOf(tooltipOf(received(player), "rare"), "Mithril").color());
    }

    @Test
    void emptyTooltipIsLeftOut() {
        new FancyMessage(MessageType.INFO, messageUtil).addTooltipped("plain", "").send(player);

        assertNull(styleOf(received(player), "plain").hoverEvent());
    }

    @Test
    void sectionSignAtTheVeryEndOfTheTextIsText() {
        new FancyMessage(MessageType.INFO, messageUtil).addSimple("abc§").send(player);

        assertEquals("[Test] abc§", plainText(received(player)));
    }

    @Test
    void hashInMessageTextIsAColourOnlyBeforeSixHexDigits() {
        new FancyMessage(MessageType.INFO, messageUtil).addSimple("Rank #1 in the list, plot #12").send(player);

        assertEquals("[Test] Rank #1 in the list, plot #12", plainText(received(player)));
    }

    /**
     * As MCME-Architect's sign editor builds its message: each line ends with a typed \n.
     */
    @Test
    void typedBackslashNInTheTextIsALineBreak() {
        FancyMessage message = new FancyMessage(MessageType.INFO, messageUtil);
        message.addSimple("You are editing sign side: FRONT\\n");
        message.addSimple("Click at a line to edit it.\\n");
        message.addFancy("[1] Welcome\\n", "/sign 1 Welcome",
                "Click to edit. Don't change the leading '/sign <line index> '.");
        message.send(player);

        assertEquals("[Test] You are editing sign side: FRONT\nClick at a line to edit it.\n[1] Welcome\n",
                plainText(received(player)));
    }

    @Test
    void commandLosesTheCharactersMinecraftDoesNotAllowInChat() {
        String command = "/warp §aSpawn\n\tnow" + (char) 127;
        new FancyMessage(MessageType.INFO, messageUtil).addClickable("suggested", command).send(player);
        new FancyMessage(MessageType.INFO, messageUtil).addClickable("run", command).setRunDirect().send(player);

        assertEquals(ClickEvent.suggestCommand("/warp aSpawnnow"),
                styleOf(received(player), "suggested").clickEvent());
        assertEquals(ClickEvent.runCommand("/warp aSpawnnow"), styleOf(received(player), "run").clickEvent());
    }

    @Test
    void copiedTextKeepsEveryCharacter() {
        String text = "§aSpawn\n\tnow";
        new FancyMessage(MessageType.INFO, messageUtil).addClickable("copy", text).setCopyToClipboard().send(player);

        assertEquals(ClickEvent.copyToClipboard(text), styleOf(received(player), "copy").clickEvent());
    }

    @ParameterizedTest
    @ValueSource(strings = {"https://www.mcmiddleearth.com/a page", "httpx://www.mcmiddleearth.com", "http:"})
    void webAddressMinecraftCannotOpenIsCopiedInsteadWithAWarning(String address) {
        Logger logger = Logger.getLogger(FancyMessage.class.getName());
        List<String> warnings = new ArrayList<>();
        Handler handler = new Handler() {
            @Override
            public void publish(LogRecord record) {
                if (record.getLevel() == Level.WARNING) {
                    warnings.add(record.getMessage());
                }
            }

            @Override
            public void flush() {
            }

            @Override
            public void close() {
            }
        };
        logger.addHandler(handler);
        try {
            new FancyMessage(MessageType.INFO, messageUtil).addFancy("website", address, "Our website").send(player);
        } finally {
            logger.removeHandler(handler);
        }

        Component message = received(player);
        assertEquals(ClickEvent.copyToClipboard(address), styleOf(message, "website").clickEvent());
        assertEquals("Our website", plainText(tooltipOf(message, "website")));
        assertEquals(1, warnings.size(), "warnings: " + warnings);
        assertTrue(warnings.get(0).contains(address), warnings.get(0));
    }

    /**
     * Without a click of its own, a later part would use the first part's click.
     */
    @Test
    void partWithAnAddressMinecraftCannotOpenDoesNotUseTheFirstPartsClick() {
        new FancyMessage(MessageType.WHITE, messageUtil)
                .addClickable("§3/cmd", "/cmd ")
                .addClickable(" our site", "https://www.mcmiddleearth.com/a page")
                .setRunDirect()
                .send(player);

        assertEquals(ClickEvent.copyToClipboard("https://www.mcmiddleearth.com/a page"),
                styleOf(received(player), " our site").clickEvent());
    }

    @Test
    void quotesAndBackslashesStayAsTyped() {
        String text = "say \"hi\" in C:\\temp";
        new FancyMessage(MessageType.INFO, messageUtil)
                .addFancy(text, "/say \"hi\" in C:\\temp", "Says \"hi\" in C:\\temp")
                .send(player);

        Component message = received(player);
        assertEquals(ClickEvent.suggestCommand("/say \"hi\" in C:\\temp"), styleOf(message, text).clickEvent());
        assertEquals("Says \"hi\" in C:\\temp", plainText(tooltipOf(message, text)));
    }

    /**
     * getData() hands out the parts themselves, so a plugin can add one without a colour or format.
     */
    @Test
    void partAddedWithoutAColourGetsTheBaseColour() {
        FancyMessage message = new FancyMessage(MessageType.INFO, messageUtil).setBaseColor(ChatColor.RED);
        message.getData().add(new String[]{"added", "/added", null});
        message.send(player);

        Style added = styleOf(received(player), "added");
        assertEquals(NamedTextColor.RED, added.color());
        assertEquals(ClickEvent.suggestCommand("/added"), added.clickEvent());
    }

    @Test
    void laterPartsUseTheFirstPartsClickWhenThereIsNoPrefix() {
        new FancyMessage(MessageType.WHITE, messageUtil)
                .addClickable("§3plan", "/plan load plan")
                .addSimple("§f the plan of the town")
                .setRunDirect()
                .send(player);

        assertEquals(ClickEvent.runCommand("/plan load plan"),
                styleOf(received(player), " the plan of the town").clickEvent());
    }

    @Test
    void baseColourComesFromTheConstructorAndSetBaseColor() {
        new FancyMessage(MessageType.INFO, messageUtil, ChatColor.GREEN)
                .addSimple("green ")
                .setBaseColor(ChatColor.RED)
                .addSimple("red")
                .send(player);

        Component message = received(player);
        assertEquals(NamedTextColor.AQUA, styleOf(message, "[Test] ").color());
        assertEquals(NamedTextColor.GREEN, styleOf(message, "green ").color());
        assertEquals(NamedTextColor.RED, styleOf(message, "red").color());
    }

    private static String withoutCodes(String text) {
        return text.replaceAll("§[0-9a-fk-or]", "");
    }

    private static Component received(PlayerMock player) {
        Component message = player.nextComponentMessage();
        assertNotNull(message, player.getName() + " got no message");
        return message;
    }
}
