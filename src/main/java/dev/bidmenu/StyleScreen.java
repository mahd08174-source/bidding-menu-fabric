package dev.bidmenu;

import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;

/** Pick the text colour and outline colour: blue (default), rainbow, a preset, or a custom hex. */
public class StyleScreen extends class_437 {
    private class_4185 textBtn;
    private class_4185 outlineBtn;
    private class_342 textHexField;
    private class_342 outlineHexField;
    private String seenText;
    private String seenOutline;

    private int left, y0;

    public StyleScreen() {
        super(class_2561.method_43470("Style"));
    }

    private static String clean(String s) {
        s = s.trim();
        return s.startsWith("#") ? s.substring(1) : s;
    }

    private static class_2561 label(String what, int mode) {
        return class_2561.method_43470(what + ": " + Style.NAMES[mode]);
    }

    private void refreshLabels() {
        textBtn.method_25355(label("Text", Style.textMode));
        outlineBtn.method_25355(label("Outline", Style.outlineMode));
    }

    @Override
    protected void method_25426() {
        class_327 tr = class_310.method_1551().field_1772;
        left = this.field_22789 / 2 - 150;
        y0 = Math.max(4, (this.field_22790 - 190) / 2);

        textBtn = this.method_37063(class_4185.method_46430(label("Text", Style.textMode), b -> {
            Style.textMode = (Style.textMode + 1) % Style.NAMES.length;
            refreshLabels();
            Style.save();
        }).method_46434(left, y0 + 36, 170, 20).method_46431());

        textHexField = new class_342(tr, left + 200, y0 + 36, 100, 20, class_2561.method_43470("Text hex"));
        textHexField.method_1852(Style.textHex);
        this.method_37063(textHexField);
        seenText = clean(textHexField.method_1882());

        outlineBtn = this.method_37063(class_4185.method_46430(label("Outline", Style.outlineMode), b -> {
            Style.outlineMode = (Style.outlineMode + 1) % Style.NAMES.length;
            refreshLabels();
            Style.save();
        }).method_46434(left, y0 + 76, 170, 20).method_46431());

        outlineHexField = new class_342(tr, left + 200, y0 + 76, 100, 20, class_2561.method_43470("Outline hex"));
        outlineHexField.method_1852(Style.outlineHex);
        this.method_37063(outlineHexField);
        seenOutline = clean(outlineHexField.method_1882());

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Done"),
                b -> class_310.method_1551().method_1507(new BidScreen()))
                .method_46434(left, y0 + 160, 146, 20).method_46431());

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Reset to blue"), b -> {
            Style.reset();
            textHexField.method_1852(Style.textHex);
            outlineHexField.method_1852(Style.outlineHex);
            seenText = clean(textHexField.method_1882());
            seenOutline = clean(outlineHexField.method_1882());
            refreshLabels();
            Style.save();
        }).method_46434(left + 154, y0 + 160, 146, 20).method_46431());
    }

    /** Typing a valid 6-digit hex switches that element to Custom. */
    private void pollHex() {
        String t = clean(textHexField.method_1882());
        if (!t.equals(seenText)) {
            seenText = t;
            if (Style.validHex(t)) {
                Style.textHex = t.toUpperCase();
                Style.textMode = Style.CUSTOM;
                refreshLabels();
                Style.save();
            }
        }
        String o = clean(outlineHexField.method_1882());
        if (!o.equals(seenOutline)) {
            seenOutline = o;
            if (Style.validHex(o)) {
                Style.outlineHex = o.toUpperCase();
                Style.outlineMode = Style.CUSTOM;
                refreshLabels();
                Style.save();
            }
        }
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25394(ctx, mouseX, mouseY, delta);
        class_327 tr = class_310.method_1551().field_1772;
        pollHex();

        Style.text(ctx, tr, "Style", left, y0);
        Style.text(ctx, tr, "Text colour (click to change)", left, y0 + 24);
        Style.text(ctx, tr, "Custom hex, e.g. 3D8BFF", left + 200, y0 + 24);
        Style.text(ctx, tr, "Outline colour (click to change)", left, y0 + 64);
        Style.text(ctx, tr, "Custom hex, e.g. FF4040", left + 200, y0 + 64);

        // live preview
        int px = left, py = y0 + 108, pw = 300, ph = 40;
        ctx.method_25294(px, py, px + pw, py + ph, 0x33000000);
        Style.border(ctx, px, py, pw, ph, 2);
        Style.text(ctx, tr, "Preview: this is how it will look", px + 10, py + 15);
    }
}
