package com.everest.soundwave.data;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.ArrayList;
import java.util.List;

public class RecentTracksPrefs {

    private static final String PREFS_NAME = "recent_tracks";
    private static final String KEY_IDS = "ids";
    private static final int MAX_SIZE = 20;
    private static final String SEP = ",";

    public static void add(Context context, long id) {
        SharedPreferences prefs = prefs(context);
        List<Long> ids = parse(prefs.getString(KEY_IDS, ""));
        ids.remove(id);
        ids.add(0, id);
        if (ids.size() > MAX_SIZE) ids = ids.subList(0, MAX_SIZE);
        prefs.edit().putString(KEY_IDS, serialize(ids)).apply();
    }

    public static List<Long> getIds(Context context) {
        return parse(prefs(context).getString(KEY_IDS, ""));
    }

    public static void clear(Context context) {
        prefs(context).edit().remove(KEY_IDS).apply();
    }

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    private static List<Long> parse(String raw) {
        List<Long> ids = new ArrayList<>();
        if (raw == null || raw.isEmpty()) return ids;
        for (String s : raw.split(SEP)) {
            try { ids.add(Long.parseLong(s.trim())); } catch (NumberFormatException ignored) {}
        }
        return ids;
    }

    private static String serialize(List<Long> ids) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ids.size(); i++) {
            if (i > 0) sb.append(SEP);
            sb.append(ids.get(i));
        }
        return sb.toString();
    }
}
