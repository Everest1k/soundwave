package com.everest.soundwave.data;

import android.content.Context;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;

import com.everest.soundwave.auth.AuthManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TrackRepository {

    private static volatile TrackRepository instance;

    private final TrackDao dao;
    private final FavoriteDao favDao;
    private final PlaylistDao playlistDao;
    private final Context appContext;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private TrackRepository(Context context) {
        AppDatabase db = AppDatabase.get(context);
        this.dao = db.trackDao();
        this.favDao = db.favoriteDao();
        this.playlistDao = db.playlistDao();
        this.appContext = context.getApplicationContext();
    }

    public static TrackRepository get(Context context) {
        if (instance == null) {
            synchronized (TrackRepository.class) {
                if (instance == null) {
                    instance = new TrackRepository(context);
                }
            }
        }
        return instance;
    }

    public LiveData<List<Track>> observeAll() {
        return dao.observeAll();
    }

    public LiveData<List<Track>> observeRecent(int limit) {
        return dao.observeRecent(limit);
    }

    public LiveData<List<Track>> search(String q) {
        if (q == null || q.trim().isEmpty()) {
            return dao.observeAll();
        }
        String lower = q.toLowerCase(Locale.getDefault());
        return Transformations.map(dao.observeAll(), tracks -> {
            List<Track> result = new ArrayList<>();
            for (Track t : tracks) {
                String titleLower = t.title != null ? t.title.toLowerCase(Locale.getDefault()) : "";
                String artistLower = t.artist != null ? t.artist.toLowerCase(Locale.getDefault()) : "";
                if (titleLower.contains(lower) || artistLower.contains(lower)) {
                    result.add(t);
                }
            }
            return result;
        });
    }

    public LiveData<List<Track>> observeByIds(List<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            MutableLiveData<List<Track>> empty = new MutableLiveData<>();
            empty.setValue(Collections.emptyList());
            return empty;
        }
        return Transformations.map(dao.getByIds(ids), tracks -> {
            if (tracks == null) return Collections.emptyList();
            Map<Long, Track> map = new HashMap<>();
            for (Track t : tracks) map.put(t.id, t);
            List<Track> ordered = new ArrayList<>();
            for (Long id : ids) {
                Track t = map.get(id);
                if (t != null) ordered.add(t);
            }
            return ordered;
        });
    }

    private String userId() {
        return AuthManager.get(appContext).currentUserId();
    }

    public LiveData<List<Track>> observeLiked() {
        return favDao.observeUserLiked(userId());
    }

    public LiveData<Integer> observeLikedCount() {
        return favDao.observeUserLikedCount(userId());
    }

    public LiveData<Boolean> observeLiked(long trackId) {
        return favDao.observeLiked(userId(), trackId);
    }

    public void setLiked(long trackId, boolean liked) {
        String uid = userId();
        io.execute(() -> {
            if (liked) {
                Favorite f = new Favorite();
                f.userId = uid;
                f.trackId = trackId;
                f.addedAt = System.currentTimeMillis();
                favDao.add(f);
            } else {
                favDao.remove(uid, trackId);
            }
        });
    }

    public void insert(Track t, java.util.function.Consumer<Long> onDone) {
        io.execute(() -> {
            long id = dao.insert(t);
            if (onDone != null) onDone.accept(id);
        });
    }

    public void delete(Track t) {
        io.execute(() -> {
            playlistDao.removeTrackFromAllPlaylists(t.id);
            favDao.deleteForTrack(t.id);
            dao.delete(t);
        });
    }

    public void rename(long id, String title) {
        io.execute(() -> dao.rename(id, title));
    }

    public List<Track> getAllSync() {
        return dao.getAllSync();
    }

    public ExecutorService io() {
        return io;
    }
}
