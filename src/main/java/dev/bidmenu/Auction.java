package dev.bidmenu;

import net.minecraft.class_1799;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** State of the current bid: what is being bid on, how long is left, and who has paid the most. */
public final class Auction {
    private Auction() {}

    private static final Pattern MONEY = Pattern.compile("^\\$?\\s*([0-9][0-9,]*(?:\\.[0-9]+)?|\\.[0-9]+)\\s*([kKmMbBtT]?)$");

    /** True once a bid has been started (and until it is cleared), so the menu stays up after it ends. */
    public static boolean exists = false;
    public static boolean running = false;

    public static class_1799 stack = null;
    public static String blockName = "";
    public static long endMillis = 0;
    public static double minBid = 0;

    /** Total paid so far by each player (bids add up if someone pays again). */
    private static final Map<String, Double> totals = new HashMap<>();
    public static double topAmount = 0;
    public static String winner = null;

    public static void start(class_1799 item, String name, int seconds, double min) {
        stack = item;
        blockName = name;
        minBid = min;
        endMillis = System.currentTimeMillis() + seconds * 1000L;
        totals.clear();
        topAmount = 0;
        winner = null;
        exists = true;
        running = true;
    }

    public static void clear() {
        exists = false;
        running = false;
        totals.clear();
        topAmount = 0;
        winner = null;
    }

    public static void finish() {
        running = false;
    }

    public static long remainingMillis() {
        return Math.max(0, endMillis - System.currentTimeMillis());
    }

    /** Called for each "X paid you $N" message while a bid is running. */
    public static void payment(String player, double amount) {
        if (!running || amount <= 0) return;
        double total = totals.merge(player, amount, Double::sum);
        if (total >= minBid && total > topAmount) {
            topAmount = total;
            winner = player;
        }
    }

    /** Parses things like 500, 1,500, 10k, 2.5m, 1b. Returns -1 if it is not a valid amount. */
    public static double parseMoney(String s) {
        Matcher m = MONEY.matcher(s.trim());
        if (!m.matches()) return -1;
        double v;
        try {
            v = Double.parseDouble(m.group(1).replace(",", ""));
        } catch (NumberFormatException e) {
            return -1;
        }
        String suffix = m.group(2).toLowerCase();
        if (suffix.equals("k")) v *= 1e3;
        else if (suffix.equals("m")) v *= 1e6;
        else if (suffix.equals("b")) v *= 1e9;
        else if (suffix.equals("t")) v *= 1e12;
        return v;
    }

    /** 1500 -> 1.5K, 2000000 -> 2M. */
    public static String format(double v) {
        String[] suf = {"", "K", "M", "B", "T"};
        int i = 0;
        while (v >= 1000 && i < suf.length - 1) {
            v /= 1000;
            i++;
        }
        String s;
        if (v == Math.floor(v)) {
            s = String.valueOf((long) v);
        } else {
            s = String.format("%.2f", v).replaceAll("0+$", "").replaceAll("\\.$", "");
        }
        return s + suf[i];
    }

    public static String clock(long millis) {
        long total = (millis + 999) / 1000;
        return (total / 60) + ":" + String.format("%02d", total % 60);
    }

    /** diamond_block -> Diamond Block */
    public static String prettyName(String path) {
        StringBuilder sb = new StringBuilder();
        for (String word : path.split("_")) {
            if (word.isEmpty()) continue;
            if (sb.length() > 0) sb.append(' ');
            sb.append(Character.toUpperCase(word.charAt(0))).append(word.substring(1));
        }
        return sb.toString();
    }
}
