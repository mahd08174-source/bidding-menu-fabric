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
 * The bidding menu: square, see-through box with a rainbow outline and rainbow text.
 * Open chat and drag it with the left mouse button to move it.
 */
public final class BidHud {
    private BidHud() {}

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("bidding-menu.properties");
    private static final int BODY = 0x33000000; // mostly see-through
    private static final int PAD = 8;
    private static final int ICON = 32;         // item drawn at 2x

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

    public static void render(class_332 ctx, class_9779 tick) {
        class_310 mc = class_310.method_1551();
        if (!Auction.exists || mc.field_1724 == null || mc.field_1690.field_1842) return;

        class_327 tr = mc.field_1772;
        class_1041 win = mc.method_22683();

        // --- content ---
        String name = Auction.blockName;
        String bidLine = "Top bid: " + (Auction.winner == null ? "none yet" : "$" + Auction.format(Auction.topAmount));
        String winnerLine = "Winner: " + (Auction.winner == null ? "-" : Auction.winner);
        String timeLine = Auction.running ? "Time left: " + Auction.clock(Auction.remainingMillis()) : "Bidding ended";

        int width = Math.max(150, Math.max(tr.method_1727(name),
                Math.max(tr.method_1727(bidLine), Math.max(tr.method_1727(winnerLine), tr.method_1727(timeLine)))) + PAD * 2);
        int height = PAD + 10 + 4 + ICON + 6 + 3 * 12 + PAD;

        if (posX == Integer.MIN_VALUE) {
            posX = (win.method_4486() - width) / 2;
            posY = 30;
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

        // --- box: see-through body + rainbow outline ---
        ctx.method_25294(x, y, x + width, y + height, BODY);
        Rainbow.border(ctx, x, y, width, height, 2);

        int cx = x + width / 2;
        int ty = y + PAD;
        Rainbow.centeredText(ctx, tr, name, cx, ty);

        // block image (2x)
        ty += 10 + 4;
        if (Auction.stack != null && !Auction.stack.method_7960()) {
            var m = ctx.method_51448();
            m.pushMatrix();
            m.translate(cx - ICON / 2f, ty);
            m.scale(2f, 2f);
            ctx.method_51427(Auction.stack, 0, 0);
            m.popMatrix();
        }

        ty += ICON + 6;
        Rainbow.centeredText(ctx, tr, bidLine, cx, ty);
        Rainbow.centeredText(ctx, tr, winnerLine, cx, ty + 12);
        Rainbow.centeredText(ctx, tr, timeLine, cx, ty + 24);

        if (chatOpen) {
            Rainbow.text(ctx, tr, "Drag to move", x, y + height + 3);
        }
    }
}
