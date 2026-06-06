package com.everest.soundwave.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.media.MediaMetadataRetriever;
import android.net.Uri;
import android.provider.OpenableColumns;

import androidx.annotation.Nullable;

import com.everest.soundwave.data.Track;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public final class TrackImporter {

    private TrackImporter() {}

    @Nullable
    public static Track importFromUri(Context ctx, Uri uri) {
        try {
            ContentResolver cr = ctx.getContentResolver();

            String displayName = queryDisplayName(cr, uri);

            File audioDir = new File(ctx.getFilesDir(), "audio");
            if (!audioDir.exists()) audioDir.mkdirs();

            String safeName = (displayName == null ? UUID.randomUUID().toString() : displayName)
                    .replaceAll("[^a-zA-Zа-яА-Я0-9._-]", "_");
            File audioFile = new File(audioDir, System.currentTimeMillis() + "_" + safeName);
            try (InputStream in = cr.openInputStream(uri);
                 OutputStream out = new FileOutputStream(audioFile)) {
                if (in == null) return null;
                byte[] buf = new byte[8192];
                int n;
                while ((n = in.read(buf)) > 0) out.write(buf, 0, n);
            }

            String title = stripExtension(displayName);
            String artist = "";
            long duration = 0;
            String coverPath = null;

            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            try {
                mmr.setDataSource(audioFile.getAbsolutePath());

                String t = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_TITLE);
                if (t != null && !t.isEmpty()) title = t;

                String a = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_ARTIST);
                if (a != null && !a.isEmpty()) artist = a;

                String d = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
                if (d != null) {
                    try { duration = Long.parseLong(d); } catch (NumberFormatException ignored) {}
                }

                byte[] art = mmr.getEmbeddedPicture();
                if (art != null && art.length > 0) {
                    File coverDir = new File(ctx.getFilesDir(), "covers");
                    if (!coverDir.exists()) coverDir.mkdirs();
                    File coverFile = new File(coverDir, System.currentTimeMillis() + "_" + UUID.randomUUID() + ".jpg");
                    try (FileOutputStream fos = new FileOutputStream(coverFile)) {
                        fos.write(art);
                    }
                    coverPath = coverFile.getAbsolutePath();
                }
            } catch (Exception ignored) {
            } finally {
                try { mmr.release(); } catch (Exception ignored) {}
            }

            if (title == null || title.isEmpty()) title = "Без названия";

            return new Track(
                    title,
                    artist,
                    Uri.fromFile(audioFile).toString(),
                    coverPath,
                    duration,
                    System.currentTimeMillis()
            );
        } catch (Exception e) {
            return null;
        }
    }

    @Nullable
    private static String queryDisplayName(ContentResolver cr, Uri uri) {
        try (Cursor c = cr.query(uri, new String[]{OpenableColumns.DISPLAY_NAME}, null, null, null)) {
            if (c != null && c.moveToFirst()) {
                int idx = c.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                if (idx >= 0) return c.getString(idx);
            }
        } catch (Exception ignored) {}
        return null;
    }

    private static String stripExtension(@Nullable String name) {
        if (name == null) return "";
        int dot = name.lastIndexOf('.');
        return dot > 0 ? name.substring(0, dot) : name;
    }
}
