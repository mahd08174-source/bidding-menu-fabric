package dev.bidmenu;

import net.minecraft.class_1792;
import net.minecraft.class_1799;
import net.minecraft.class_2561;
import net.minecraft.class_310;
import net.minecraft.class_327;
import net.minecraft.class_332;
import net.minecraft.class_342;
import net.minecraft.class_4185;
import net.minecraft.class_437;
import net.minecraft.class_7923;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Setup screen: search the block list, pick one, set the duration (and a minimum bid), then Start bid.
 * Scroll the list with the mouse wheel.
 */
public class BidScreen extends class_437 {
    private record Entry(class_1799 stack, String name, String path) {}

    private static List<Entry> ALL = null;

    private static String lastTime = "30";
    private static String lastMin = "0";
    private static String selectedPath = "diamond_block";

    private static final int ROWS = 9;
    private static final int ROW_H = 18;
    private static final int LIST_W = 382;

    private class_342 searchField;
    private class_342 timeField;
    private class_342 minField;

    private List<Entry> filtered = new ArrayList<>();
    private String lastQuery = null;
    private int scroll = 0;
    private boolean lastDown = true; // true so a click that opened the screen never selects a row
    private String error = "";

    private int left, y0, fieldY, listY;

    public BidScreen() {
        super(class_2561.method_43470("Bidding Menu"));
    }

    private static void ensureItems() {
        if (ALL != null) return;
        List<Entry> list = new ArrayList<>();
        for (class_1792 item : class_7923.field_41178) {
            class_1799 stack = new class_1799(item);
            if (stack.method_7960()) continue; // air
            String name = stack.method_7964().getString();
            String path = class_7923.field_41178.method_10221(item).method_12832();
            list.add(new Entry(stack, name, path));
        }
        list.sort(Comparator.comparing((Entry e) -> e.name().toLowerCase()));
        ALL = list;
    }

    @Override
    protected void method_25426() {
        ensureItems();
        class_327 tr = class_310.method_1551().field_1772;
        int cx = this.field_22789 / 2;
        left = cx - LIST_W / 2;
        y0 = Math.max(4, (this.field_22790 - 270) / 2);
        fieldY = y0 + 28;
        listY = fieldY + 26;

        searchField = new class_342(tr, left, fieldY, 190, 20, class_2561.method_43470("Search"));
        this.method_37063(searchField);

        timeField = new class_342(tr, left + 196, fieldY, 104, 20, class_2561.method_43470("Duration"));
        timeField.method_1852(lastTime);
        this.method_37063(timeField);

        minField = new class_342(tr, left + 306, fieldY, 76, 20, class_2561.method_43470("Min bid"));
        minField.method_1852(lastMin);
        this.method_37063(minField);

        int by = listY + ROWS * ROW_H + 40;
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Start bid"), b -> start())
                .method_46434(left, by, 120, 20).method_46431());
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Stop"), b -> {
                    Auction.clear();
                    tell("Bidding stopped.");
                })
                .method_46434(left + 126, by, 80, 20).method_46431());
        this.method_37063(class_4185.method_46430(class_2561.method_43470("Close"), b -> closeScreen())
                .method_46434(left + 212, by, 80, 20).method_46431());

        lastQuery = null; // forces the list to be built on the first render
    }

    private void closeScreen() {
        class_310.method_1551().method_1507(null);
    }

    private static void tell(String message) {
        class_310 mc = class_310.method_1551();
        if (mc.field_1724 != null) {
            mc.field_1724.method_7353(class_2561.method_43470(message), false);
        }
    }

    private void refilter(String q) {
        List<Entry> out = new ArrayList<>();
        for (Entry e : ALL) {
            if (q.isEmpty() || e.name().toLowerCase().contains(q) || e.path().contains(q)) out.add(e);
        }
        filtered = out;
        scroll = 0;
    }

    private int maxScroll() {
        return Math.max(0, filtered.size() - ROWS);
    }

    private boolean inList(double mx, double my) {
        return mx >= left && mx < left + LIST_W && my >= listY && my < listY + ROWS * ROW_H;
    }

    @Override
    public boolean method_25401(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        if (inList(mouseX, mouseY)) {
            scroll = Math.max(0, Math.min(maxScroll(), scroll - (int) Math.signum(verticalAmount) * 3));
            return true;
        }
        return super.method_25401(mouseX, mouseY, horizontalAmount, verticalAmount);
    }

    private void start() {
        String timeText = timeField.method_1882().trim();
        String minText = minField.method_1882().trim();

        Entry chosen = null;
        for (Entry e : ALL) {
            if (e.path().equals(selectedPath)) {
                chosen = e;
                break;
            }
        }
        if (chosen == null) {
            error = "Pick a block from the list first";
            return;
        }

        int seconds;
        try {
            seconds = Integer.parseInt(timeText);
        } catch (NumberFormatException e) {
            seconds = -1;
        }
        if (seconds < 1 || seconds > 86400) {
            error = "Duration must be a whole number from 1 to 86400";
            return;
        }

        double min = minText.isEmpty() ? 0 : Auction.parseMoney(minText);
        if (min < 0) {
            error = "Min bid must be a number, like 5000 or 10k";
            return;
        }

        lastTime = timeText;
        lastMin = minText.isEmpty() ? "0" : minText;

        Auction.start(chosen.stack(), chosen.name(), seconds, min);
        tell("Bidding started: " + chosen.name() + " for " + seconds + "s, min bid $" + Auction.format(min)
                + ". Payments are read from 'X paid you' messages.");
        closeScreen();
    }

    @Override
    public void method_25394(class_332 ctx, int mouseX, int mouseY, float delta) {
        super.method_25394(ctx, mouseX, mouseY, delta);
        class_310 mc = class_310.method_1551();
        class_327 tr = mc.field_1772;

        String q = searchField.method_1882().trim().toLowerCase();
        if (!q.equals(lastQuery)) {
            lastQuery = q;
            refilter(q);
        }
        scroll = Math.max(0, Math.min(maxScroll(), scroll));

        // title + labels
        Rainbow.text(ctx, tr, "Bidding Menu", left, y0);
        Rainbow.text(ctx, tr, "Search block", left, fieldY - 12);
        Rainbow.text(ctx, tr, "Duration (seconds)", left + 196, fieldY - 12);
        Rainbow.text(ctx, tr, "Min bid", left + 306, fieldY - 12);

        // list panel: see-through body, square rainbow outline
        int listH = ROWS * ROW_H;
        ctx.method_25294(left, listY, left + LIST_W, listY + listH, 0x66000000);
        Rainbow.border(ctx, left - 2, listY - 2, LIST_W + 4, listH + 4, 1);

        boolean down = GLFW.glfwGetMouseButton(mc.method_22683().method_4490(), GLFW.GLFW_MOUSE_BUTTON_LEFT) == GLFW.GLFW_PRESS;
        boolean clicked = down && !lastDown;
        lastDown = down;

        for (int r = 0; r < ROWS; r++) {
            int idx = scroll + r;
            if (idx >= filtered.size()) break;
            Entry e = filtered.get(idx);
            int ry = listY + r * ROW_H;

            boolean hover = inList(mouseX, mouseY) && mouseY >= ry && mouseY < ry + ROW_H;
            if (hover && clicked) {
                selectedPath = e.path();
                error = "";
            }
            boolean selected = e.path().equals(selectedPath);
            if (selected) {
                ctx.method_25294(left, ry, left + LIST_W, ry + ROW_H - 1, 0x55FFFFFF);
            } else if (hover) {
                ctx.method_25294(left, ry, left + LIST_W, ry + ROW_H - 1, 0x22FFFFFF);
            }
            ctx.method_51427(e.stack(), left + 4, ry + 1);
            ctx.method_51433(tr, e.name(), left + 26, ry + 5, 0xFFFFFFFF, false);
            ctx.method_25294(left + 4, ry + ROW_H - 1, left + LIST_W - 4, ry + ROW_H, 0x22FFFFFF);
        }

        // counter like "1-9 of 1505" and the current pick
        int total = filtered.size();
        String counter = total == 0
                ? "No matches"
                : (scroll + 1) + "-" + Math.min(total, scroll + ROWS) + " of " + total;
        int cy = listY + listH + 6;
        ctx.method_51433(tr, counter, left, cy, 0xFFB0B0B0, false);

        String pick = "Selected: -";
        for (Entry e : ALL) {
            if (e.path().equals(selectedPath)) {
                pick = "Selected: " + e.name();
                break;
            }
        }
        Rainbow.text(ctx, tr, pick, left, cy + 14);

        if (!error.isEmpty()) {
            ctx.method_51433(tr, error, left, cy + 28, 0xFFFF5555, false);
        }
    }
}
