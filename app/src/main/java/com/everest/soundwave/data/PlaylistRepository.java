package com.everest.soundwave.data;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.everest.soundwave.auth.AuthManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class PlaylistRepository {

    private static volatile PlaylistRepository instance;

    private final PlaylistDao dao;
    private final Context appContext;
    private final ExecutorService io = Executors.newSingleThreadExecutor();

    private PlaylistRepository(Context context) {
        this.appContext = context.getApplicationContext();
        this.dao = AppDatabase.get(appContext).playlistDao();
    }

    public static PlaylistRepository get(Context context) {
        if (instance == null) {
            synchronized (PlaylistRepository.class) {
                if (instance == null) {
                    instance = new PlaylistRepository(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private String ownerId() {
        return AuthManager.get(appContext).currentUserId();
    }

    public LiveData<List<PlaylistWithCount>> observeAll() {
        return dao.observeAllForOwner(ownerId());
    }

    public LiveData<Integer> observeCount() {
        return dao.observeCountForOwner(ownerId());
    }

    public LiveData<List<Track>> observeTracks(long playlistId) {
        return dao.observeTracks(playlistId);
    }

    public void create(String name) {
        final String owner = ownerId();
        io.execute(() -> {
            Playlist p = new Playlist();
            p.name = name;
            p.createdAt = System.currentTimeMillis();
            p.ownerId = owner;
            dao.insert(p);
        });
    }

    public void delete(Playlist p) {
        io.execute(() -> dao.delete(p));
    }

    public void addTrack(long playlistId, long trackId) {
        io.execute(() -> {
            PlaylistTrack pt = new PlaylistTrack();
            pt.playlistId = playlistId;
            pt.trackId = trackId;
            pt.addedAt = System.currentTimeMillis();
            dao.insertTrack(pt);
        });
    }

    public void removeTrack(long playlistId, long trackId) {
        io.execute(() -> {
            PlaylistTrack pt = new PlaylistTrack();
            pt.playlistId = playlistId;
            pt.trackId = trackId;
            dao.deleteTrack(pt);
        });
    }
}
