package dev.bidmenu;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_2960;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7923;

/** Setup screen: which block, how many seconds, and the minimum bid. */
public class BidScreen extends class_437 {
    private static String lastBlock = "diamond_block";
    private static String lastTime = "60";
    private static String lastMin = "0";

    private class_342 blockField;
    private class_342 timeField;
    private class_342 minField;
    private String error = "";

    public BidScreen() {
        super(class_2561.method_43470("Bidding Menu"));
    }

    @Override
    protected void method_25426() {
        class_327 tr = class_310.method_1551().field_1772;
        int cx = this.field_22789 / 2;
        int y0 = this.field_22790 / 2 - 70;

        blockField = new class_342(tr, cx - 100, y0 + 26, 200, 20, class_2561.method_43470("Block"));
        blockField.method_1852(lastBlock);
        this.method_37063(blockField);

        timeField = new class_342(tr, cx - 100, y0 + 62, 200, 20, class_2561.method_43470("Seconds"));
        timeField.method_1852(lastTime);
        this.method_37063(timeField);

        minField = new class_342(tr, cx - 100, y0 + 98, 200, 20, class_2561.method_43470("Minimum bid"));
        minField.method_1852(lastMin);
        this.method_37063(minField);

        this.method_37063(class_4185.method_46430(class_2561.method_43470("Start"), b -> start())
                .method_46434(cx - 100, y0 + 136, 98, 20).method_46431());
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Cancel"), b -> close())
                .method_46434(cx + 2, y0 + 136, 98, 20).method_46431());

        if (Auction.exists) {
            this.method_37063(class_4185.method_46430(class_2561.method_43470(Auction.running ? "Stop and clear menu" : "Clear menu"), b -> {
                        Auction.clear();
                        close();
                    })
                    .method_46434(cx - 100, y0 + 160, 200, 20).method_46431());
        }
    }

    private void close() {
        class_310.method_1551().method_1507(null);
    }

    private void start() {
        String blockText = blockField.method_1882().trim().toLowerCase();
        String timeText = timeField.method_1882().trim();
        String minText = minField.method_1882().trim();

        if (blockText.isEmpty()) {
            error = "Enter a block, like diamond_block";
            return;
        }
        if (!blockText.contains(":")) blockText = "minecraft:" + blockText;
        class_2960 id = class_2960.method_12829(blockText);
        // method_63535 = Registry.getValue(Identifier); unknown ids give air, which we reject below
        class_1792 item = id == null ? null : class_7923.field_41178.method_63535(id);
        class_1799 stack = item == null ? null : new class_1799(item);
        if (stack == null || stack.method_7960()) {
            error = "Unknown block: " + blockField.method_1882().trim();
            return;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(timeText);
        } catch (NumberFormatException e) {
            seconds = -1;
        }
        if (seconds < 1 || seconds > 86400) {
            error = "Seconds must be a whole number from 1 to 86400";
            return;
        }

        double min = minText.isEmpty() ? 0 : Auction.parseMoney(minText);
        if (min < 0) {
            error = "Minimum bid must be a number, like 5000 or 10k";
            return;
        }

        lastBlock = blockField.method_1882().trim();
        lastTime = timeText;
        lastMin = minText.isEmpty() ? "0" : minText;

        Auction.start(stack, Auction.prettyName(id.method_12832()), seconds, min);

        if (class_310.method_1551().field_1724 != null) {
            class_310.method_1551().field_1724.method_7353(class_2561.method_43470(
                    "Bidding started: " + Auction.blockName + " for " + seconds + "s, minimum $" + Auction.format(min)
                            + ". Payments are read from 'X paid you $N' messages."), false);
        }
        close();
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25394(ctx, mouseX, mouseY, delta);
        class_327 tr = class_310.method_1551().field_1772;
        int cx = this.field_22789 / 2;
        int y0 = this.field_22790 / 2 - 70;

        Rainbow.centeredText(ctx, tr, "BIDDING MENU", cx, y0);
        Rainbow.text(ctx, tr, "Block (e.g. diamond_block)", cx - 100, y0 + 14);
        Rainbow.text(ctx, tr, "Time (seconds)", cx - 100, y0 + 50);
        Rainbow.text(ctx, tr, "Minimum bid (e.g. 5000, 10k, 1.5m)", cx - 100, y0 + 86);

        if (!error.isEmpty()) {
            ctx.method_51433(tr, error, cx - tr.method_1727(error) / 2, y0 + 122, 0xFFFF5555, false);
        }
    }
}
