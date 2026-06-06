package com.everest.soundwave.remote;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.everest.soundwave.data.AppDatabase;
import com.everest.soundwave.data.Track;
import com.everest.soundwave.data.TrackDao;
import com.everest.soundwave.data.TrackRepository;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

// синхронизация треков с Яндекс.Диска
public final class YandexDiskLoader {

    public interface Callback {
        void onResult(int added, String error);
    }

    private static final String API =
            "https://cloud-api.yandex.net/v1/disk/public/resources";

    private static final ExecutorService io = Executors.newSingleThreadExecutor();
    private static final Handler ui = new Handler(Looper.getMainLooper());

    private YandexDiskLoader() {}

    public static void sync(Context ctx, Callback cb) {
        if (!RemoteTracksConfig.isConfigured()) {
            if (cb != null) cb.onResult(0,
                    "Ссылка на Яндекс.Диск не задана. " +
                            "Открой файл RemoteTracksConfig.java и вставь публичную ссылку.");
            return;
        }
        final Context appCtx = ctx.getApplicationContext();
        io.execute(() -> {
            try {
                String publicKey = RemoteTracksConfig.YANDEX_DISK_PUBLIC_LINK.trim();
                String url = API
                        + "?public_key=" + URLEncoder.encode(publicKey, "UTF-8")
                        + "&limit=500"
                        + "&preview_size=L";

                JSONObject root = httpGetJson(url);
                Log.d(TAG, "Yandex API response keys: " + root.names());

                JSONArray items = null;
                JSONObject embedded = root.optJSONObject("_embedded");
                if (embedded != null) {
                    items = embedded.optJSONArray("items");
                }
                if (items == null) {
                    items = root.optJSONArray("items");
                }
                if (items == null || items.length() == 0) {
                    post(cb, 0, "В папке нет файлов или ссылка неверна.");
                    return;
                }
                Log.d(TAG, "Yandex items count: " + items.length());

                // карта обложек: имя_без_расширения → url картинки
                Map<String, String> coverByBaseName = new HashMap<>();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject it = items.getJSONObject(i);
                    if (!"file".equals(it.optString("type"))) continue;
                    String name = it.optString("name", "");
                    String lower = name.toLowerCase();
                    boolean isImage = lower.endsWith(".jpg") || lower.endsWith(".jpeg")
                            || lower.endsWith(".png") || lower.endsWith(".webp");
                    if (!isImage) continue;
                    String fileUrl = it.optString("file", "");
                    if (fileUrl.isEmpty()) continue;
                    coverByBaseName.put(stripExt(name).toLowerCase(), fileUrl);
                }
                Log.d(TAG, "Cover candidates: " + coverByBaseName.size());

                TrackRepository repo = TrackRepository.get(appCtx);
                TrackDao trackDao = AppDatabase.get(appCtx).trackDao();

                int added = 0;
                long now = System.currentTimeMillis();
                for (int i = 0; i < items.length(); i++) {
                    JSONObject it = items.getJSONObject(i);
                    String type = it.optString("type");
                    if (!"file".equals(type)) continue;

                    String name = it.optString("name", "");
                    String mediaType = it.optString("media_type", "");
                    String lower = name.toLowerCase();
                    boolean isAudio = "audio".equals(mediaType)
                            || lower.endsWith(".mp3")
                            || lower.endsWith(".m4a")
                            || lower.endsWith(".wav")
                            || lower.endsWith(".ogg")
                            || lower.endsWith(".flac");
                    if (!isAudio) continue;

                    String fileUrl = it.optString("file", "");
                    if (fileUrl.isEmpty()) continue;

                    String path = it.optString("path", "");
                    String remoteId = path.isEmpty() ? fileUrl : path;

                    // дедуп по пути файла
                    if (trackDao.findByRemoteId(remoteId) != null) continue;

                    String preview = it.optString("preview", null);
                    if (preview != null && preview.isEmpty()) preview = null;

                    String matchedCover = coverByBaseName.get(stripExt(name).toLowerCase());
                    if (matchedCover != null) preview = matchedCover;

                    String title = stripExt(name);
                    String artist = "";
                    if (title.contains(" - ")) {
                        String[] parts = title.split(" - ", 2);
                        artist = parts[0].trim();
                        title = parts[1].trim();
                    }

                    Track t = new Track(title, artist, fileUrl, preview, 0L, now + i);
                    t.remoteId = remoteId;
                    repo.insert(t, null);
                    added++;
                }

                final int addedFinal = added;
                Log.d(TAG, "Yandex sync done, added " + addedFinal);
                post(cb, addedFinal, null);
            } catch (Exception e) {
                String msg = e.getClass().getName() + ": " + e.getMessage();
                Log.e(TAG, "Yandex sync failed -> " + msg, e);
                post(cb, 0, "Ошибка загрузки: " + msg);
            }
        });
    }

    private static final String TAG = "YandexDiskLoader";

    private static void post(Callback cb, int n, String err) {
        if (cb == null) return;
        ui.post(() -> cb.onResult(n, err));
    }

    private static JSONObject httpGetJson(String urlStr) throws Exception {
        Log.d(TAG, "GET " + urlStr);
        HttpURLConnection conn = (HttpURLConnection) new URL(urlStr).openConnection();
        try {
            conn.setRequestMethod("GET");
            conn.setConnectTimeout(15000);
            conn.setReadTimeout(20000);
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Soundwave/1.0 (Android)");
            int code = conn.getResponseCode();
            InputStream stream = (code >= 200 && code < 300)
                    ? conn.getInputStream()
                    : conn.getErrorStream();
            String body = readAll(stream);
            if (code < 200 || code >= 300) {
                throw new RuntimeException("HTTP " + code + ": " + body);
            }
            return new JSONObject(body);
        } finally {
            conn.disconnect();
        }
    }

    private static String readAll(InputStream is) throws Exception {
        if (is == null) return "";
        StringBuilder sb = new StringBuilder();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(is))) {
            String line;
            while ((line = r.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private static String stripExt(String s) {
        if (s == null) return "";
        int dot = s.lastIndexOf('.');
        return dot > 0 ? s.substring(0, dot) : s;
    }
}
