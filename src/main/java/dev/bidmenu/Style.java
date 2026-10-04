package dev.bidmenu;

import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.class_327;
import net.minecraft.class_332;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;

/**
 * Player-chosen look for the menu: text colour and outline colour.
 * Default is blue for both. Each can also be Rainbow, a preset colour, or a custom hex colour.
 */
public final class Style {
    private Style() {}

    public static final String[] NAMES = {
            "Blue", "Rainbow", "White", "Red", "Green", "Yellow", "Orange", "Purple", "Pink", "Cyan", "Custom"
    };
    private static final int[] COLORS = {
            0x3D8BFF, 0, 0xFFFFFF, 0xFF4040, 0x40FF70, 0xFFE040, 0xFF9A30, 0xB060FF, 0xFF70C0, 0x40E0FF, 0
    };
    public static final int BLUE = 0;
    public static final int RAINBOW = 1;
    public static final int CUSTOM = 10;
    public static final String DEFAULT_HEX = "3D8BFF";

    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("bidding-menu-style.properties");

    public static int textMode = BLUE;
    public static int outlineMode = BLUE;
    public static String textHex = DEFAULT_HEX;
    public static String outlineHex = DEFAULT_HEX;

    public static boolean validHex(String s) {
        return s != null && s.matches("[0-9a-fA-F]{6}");
    }

    /** Solid ARGB colour for a mode (not meaningful for Rainbow). */
    public static int color(int mode, String hex) {
        if (mode == CUSTOM) {
            return validHex(hex) ? (0xFF000000 | Integer.parseInt(hex, 16)) : (0xFF000000 | COLORS[BLUE]);
        }
        return 0xFF000000 | COLORS[mode];
    }

    /** Draws text in the chosen text style. */
    public static void text(class_332 ctx, class_327 tr, String s, int x, int y) {
        if (textMode == RAINBOW) {
            Rainbow.text(ctx, tr, s, x, y);
        } else {
            ctx.method_51433(tr, s, x, y, color(textMode, textHex), false);
        }
    }

    /** Square outline drawn just inside the given box, in the chosen outline style. */
    public static void border(class_332 ctx, int x, int y, int w, int h, int t) {
        if (outlineMode == RAINBOW) {
            Rainbow.border(ctx, x, y, w, h, t);
        } else {
            int c = color(outlineMode, outlineHex);
            ctx.method_25294(x, y, x + w, y + t, c);               // top
            ctx.method_25294(x, y + h - t, x + w, y + h, c);       // bottom
            ctx.method_25294(x, y + t, x + t, y + h - t, c);       // left
            ctx.method_25294(x + w - t, y + t, x + w, y + h - t, c); // right
        }
    }

    public static void reset() {
        textMode = BLUE;
        outlineMode = BLUE;
        textHex = DEFAULT_HEX;
        outlineHex = DEFAULT_HEX;
    }

    public static void load() {
        try {
            if (!Files.exists(FILE)) return;
            Properties p = new Properties();
            try (var in = Files.newInputStream(FILE)) {
                p.load(in);
            }
            textMode = clampMode(p.getProperty("textMode"));
            outlineMode = clampMode(p.getProperty("outlineMode"));
            String th = p.getProperty("textHex");
            String oh = p.getProperty("outlineHex");
            if (validHex(th)) textHex = th.toUpperCase();
            if (validHex(oh)) outlineHex = oh.toUpperCase();
        } catch (Exception ignored) {
        }
    }

    private static int clampMode(String s) {
        try {
            int v = Integer.parseInt(s);
            return (v >= 0 && v < NAMES.length) ? v : BLUE;
        } catch (Exception e) {
            return BLUE;
        }
    }

    public static void save() {
        try {
            Properties p = new Properties();
            p.setProperty("textMode", String.valueOf(textMode));
            p.setProperty("outlineMode", String.valueOf(outlineMode));
            p.setProperty("textHex", textHex);
            p.setProperty("outlineHex", outlineHex);
            try (var out = Files.newOutputStream(FILE)) {
                p.store(out, "Bidding Menu style");
            }
        } catch (Exception ignored) {
        }
    }
}
