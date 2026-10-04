package dev.bidmenu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_1041;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_408;
import net.minecraft.class_9779;
import org.lwjgl.glfw.GLFW;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * The bidding menu: square, see-through box. Colours come from Style (blue by default, rainbow optional).
 * Layout: block icon + item name as the title, then top bid and time. Open chat and drag it with the left mouse button.
 */
public final class BidHud {
    private BidHud() {}

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("bidding-menu.properties");
    private static final int BODY = 0x33000000; // mostly see-through
    private static final int PAD = 7;

    private static int posX = Integer.MIN_VALUE;
    private static int posY = Integer.MIN_VALUE;

    private static boolean dragging = false;
    private static int dragDX, dragDY;
    private static boolean lastDown = false;

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(FILE)) {
                p.load(in);
            }
            if (p.getProperty("x") != null && p.getProperty("y") != null) {
                posX = Integer.parseInt(p.getProperty("x"));
                posY = Integer.parseInt(p.getProperty("y"));
            }
        } catch (Exception ignored) {
        }
    }

    private static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("x", String.valueOf(posX));
            p.setProperty("y", String.valueOf(posY));
            try (var out = Files.newOutputStream(FILE)) {
                p.store(out, "Bidding Menu position");
            }
        } catch (Exception ignored) {
        }
    }

    /** 00:30 style. */
    private static String clock(long millis) {
        long total = (millis + 999) / 1000;
        return String.format("%02d:%02d", total / 60, total % 60);
    }

    public static void render(class_332 ctx, class_9779 tick) {
        class_310 mc = class_310.method_1551();
        if (!Auction.exists || mc.field_1724 == null || mc.field_1690.field_1842) return;

        class_327 tr = mc.field_1772;
        class_1041 win = mc.method_22683();

        // --- content ---
        String name = Auction.blockName;
        String bidLine;
        if (Auction.running) {
            bidLine = "Top bid: " + (Auction.winner == null
                    ? "Waiting for a bid"
                    : "$" + Auction.format(Auction.topAmount) + " - " + Auction.winner);
        } else {
            bidLine = Auction.winner == null
                    ? "No winner"
                    : "Winner: " + Auction.winner + " - $" + Auction.format(Auction.topAmount);
        }
        String timeLine = "Time: " + (Auction.running ? clock(Auction.remainingMillis()) : "ended");

        int textW = Math.max(16 + 4 + tr.method_1727(name),
                Math.max(tr.method_1727(bidLine), tr.method_1727(timeLine)));
        int width = Math.max(150, textW + PAD * 2);
        // icon/title row 16 + gap 6 + bid 10 + gap 4 + time 10
        int height = PAD + 16 + 6 + 10 + 4 + 10 + PAD;

        if (posX == Integer.MIN_VALUE) {
            posX = 8;
            posY = 40;
        }

        // --- dragging (only while chat is open, so the cursor is free) ---
        boolean chatOpen = mc.field_1755 instanceof class_408;
        double mouseX = mc.field_1729.method_1603() * win.method_4486() / (double) win.method_4480();
        double mouseY = mc.field_1729.method_1604() * win.method_4502() / (double) win.method_4507();
        boolean down = chatOpen && GLFW.glfwGetMouseButton(win.method_4490(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean hover = chatOpen && mouseX >= posX && mouseX < posX + width && mouseY >= posY && mouseY < posY + height;
        if (down && !lastDown && hover) {
            dragging = true;
            dragDX = (int) mouseX - posX;
            dragDY = (int) mouseY - posY;
        }
        if (dragging && down) {
            posX = Math.max(0, Math.min((int) mouseX - dragDX, win.method_4486() - width));
            posY = Math.max(0, Math.min((int) mouseY - dragDY, win.method_4502() - height));
        }
        if (dragging && !down) {
            dragging = false;
            save();
        }
        lastDown = down;

        int x = posX, y = posY;

        // --- box: see-through body + square outline in the chosen style ---
        ctx.method_25294(x, y, x + width, y + height, BODY);
        Style.border(ctx, x, y, width, height, 2);

        int tx = x + PAD;
        int ty = y + PAD;

        // title row: block icon + item name
        if (Auction.stack != null && !Auction.stack.method_7960()) {
            ctx.method_51427(Auction.stack, tx, ty);
        }
        Style.text(ctx, tr, name, tx + 16 + 4, ty + 4);

        ty += 16 + 6;
        Style.text(ctx, tr, bidLine, tx, ty);
        Style.text(ctx, tr, timeLine, tx, ty + 14);

        if (chatOpen) {
            Style.text(ctx, tr, "Drag to move", x, y + height + 3);
        }
    }
}
