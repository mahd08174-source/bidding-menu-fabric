package dev.bidmenu;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Press B (with no screen open) to set up a bid. While it runs, every "X paid you $N" message
 * counts toward X's bid. When time runs out the highest bidder is the winner.
 */
public class BidMod implements ClientModInitializer {
    public static final Logger LOGGER = LoggerFactory.getLogger("Bidding Menu");

    /** Matches e.g. "Steve paid you $2.5M" (rank prefixes before the name are ignored). */
    private static final Pattern PAID = Pattern.compile(
            "(\\w{1,16}) paid you \\$?([0-9][0-9,]*(?:\\.[0-9]+)?)\\s*([kKmMbBtT]?)");

    private static boolean lastKeyDown = false;

    @Override
    public void onInitializeClient() {
        Style.load();
        BidHud.load();
        HudElementRegistry.addLast(class_2960.method_60655("bidding-menu", "main"), BidHud::render);
        ClientTickEvents.END_CLIENT_TICK.register(BidMod::tick);
        ClientReceiveMessageEvents.GAME.register((message, overlay) -> {
            if (!overlay) onMessage(message.getString());
        });
    }

    private static void onMessage(String text) {
        if (!Auction.running) return;
        Matcher m = PAID.matcher(text);
        if (m.find()) {
            double amount = Auction.parseMoney(m.group(2) + m.group(3));
            if (amount > 0) {
                Auction.payment(m.group(1), amount);
            }
        } else if (text.contains("$")) {
            // helps debugging if the server's payment message looks different from what we expect
            LOGGER.info("Unmatched money message: {}", text);
        }
    }

    private static void tick(class_310 mc) {
        if (mc.method_22683() == null) return;

        boolean down = GLFW.glfwGetKey(mc.method_22683().method_4490(), GLFW.GLFW_KEY_B) == GLFW.GLFW_PRESS;
        if (down && !lastKeyDown && mc.field_1755 == null && mc.field_1724 != null) {
            mc.method_1507(new BidScreen());
        }
        lastKeyDown = down;

        if (Auction.running && Auction.remainingMillis() <= 0) {
            Auction.finish();
            if (mc.field_1724 != null) {
                String result = Auction.winner == null
                        ? "Bidding over - no bid reached the minimum."
                        : "Bidding over! Winner: " + Auction.winner + " with $" + Auction.format(Auction.topAmount);
                mc.field_1724.method_7353(class_2561.method_43470(result), false);
            }
        }
    }
}
