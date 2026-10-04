package dev.bidmenu;

import net.minecraft.class_327;
import net.minecraft.class_332;

/** Rainbow colour helpers: animated hue, rainbow text and a rainbow outline. */
public final class Rainbow {
    private Rainbow() {}

    /** 0..1, loops every 4 seconds. */
    public static float time() {
        return (System.currentTimeMillis() % 4000L) / 4000f;
    }

    public static int hsv(float h, float s, float v) {
        h = h - (float) Math.floor(h);
        float c = v * s;
        float x = c * (1f - Math.abs((h * 6f) % 2f - 1f));
        float m = v - c;
        float r, g, b;
        int seg = (int) (h * 6f);
        if (seg == 0)      { r = c; g = x; b = 0; }
        else if (seg == 1) { r = x; g = c; b = 0; }
        else if (seg == 2) { r = 0; g = c; b = x; }
        else if (seg == 3) { r = 0; g = x; b = c; }
        else if (seg == 4) { r = x; g = 0; b = c; }
        else               { r = c; g = 0; b = x; }
        int ri = (int) ((r + m) * 255f + 0.5f);
        int gi = (int) ((g + m) * 255f + 0.5f);
        int bi = (int) ((b + m) * 255f + 0.5f);
        return 0xFF000000 | (ri << 16) | (gi << 8) | bi;
    }

    /** Draws text with a moving rainbow, one colour per character. */
    public static void text(class_332 ctx, class_327 tr, String s, int x, int y) {
        float base = time();
        int cx = x;
        for (int i = 0; i < s.length(); i++) {
            String ch = String.valueOf(s.charAt(i));
            ctx.method_51433(tr, ch, cx, y, hsv(base + i * 0.05f, 0.75f, 1f), false);
            cx += tr.method_1727(ch);
        }
    }

    public static void centeredText(class_332 ctx, class_327 tr, String s, int centerX, int y) {
        text(ctx, tr, s, centerX - tr.method_1727(s) / 2, y);
    }

    /** Square (unrounded) rainbow outline drawn just inside the given box. */
    public static void border(class_332 ctx, int x, int y, int w, int h, int t) {
        float base = time();
        int per = 2 * (w + h);
        for (int i = 0; i < w; i += 2) { // top, left -> right
            int len = Math.min(2, w - i);
            ctx.method_25294(x + i, y, x + i + len, y + t, hsv(base + (float) i / per, 0.8f, 1f));
        }
        for (int j = 0; j < h; j += 2) { // right, top -> bottom
            int len = Math.min(2, h - j);
            ctx.method_25294(x + w - t, y + j, x + w, y + j + len, hsv(base + (float) (w + j) / per, 0.8f, 1f));
        }
        for (int i = 0; i < w; i += 2) { // bottom, right -> left
            int len = Math.min(2, w - i);
            int x2 = x + w - i;
            ctx.method_25294(x2 - len, y + h - t, x2, y + h, hsv(base + (float) (w + h + i) / per, 0.8f, 1f));
        }
        for (int j = 0; j < h; j += 2) { // left, bottom -> top
            int len = Math.min(2, h - j);
            int y2 = y + h - j;
            ctx.method_25294(x, y2 - len, x + t, y2, hsv(base + (float) (2 * w + h + j) / per, 0.8f, 1f));
        }
    }
}
