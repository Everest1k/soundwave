package com.everest.soundwave.player;

import android.content.Context;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.everest.soundwave.data.Track;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PlayerManager {

    private static volatile PlayerManager instance;

    private final Context appContext;
    private MediaPlayer player;

    private final List<Track> queue = new ArrayList<>();
    private int currentIndex = -1;

    private final MutableLiveData<Track> currentTrack = new MutableLiveData<>(null);
    private final MutableLiveData<Boolean> isPlaying = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> progressMs = new MutableLiveData<>(0);
    private final MutableLiveData<Integer> durationMs = new MutableLiveData<>(0);

    private final Handler mainHandler = new Handler(Looper.getMainLooper());
    private final Runnable progressTicker = new Runnable() {
        @Override public void run() {
            if (player != null && Boolean.TRUE.equals(isPlaying.getValue())) {
                try {
                    progressMs.setValue(player.getCurrentPosition());
                } catch (IllegalStateException ignored) {}
                mainHandler.postDelayed(this, 500);
            }
        }
    };

    private PlayerManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static PlayerManager get(Context context) {
        if (instance == null) {
            synchronized (PlayerManager.class) {
                if (instance == null) instance = new PlayerManager(context);
            }
        }
        return instance;
    }

    public LiveData<Track> currentTrack() { return currentTrack; }
    public LiveData<Boolean> isPlaying() { return isPlaying; }
    public LiveData<Integer> progressMs() { return progressMs; }
    public LiveData<Integer> durationMs() { return durationMs; }

    public void setQueue(List<Track> tracks, int startIndex) {
        queue.clear();
        if (tracks != null) queue.addAll(tracks);
        if (queue.isEmpty()) {
            currentIndex = -1;
            stop();
            return;
        }
        currentIndex = Math.max(0, Math.min(startIndex, queue.size() - 1));
        play(queue.get(currentIndex));
    }

    public void playSingle(Track t) {
        setQueue(Collections.singletonList(t), 0);
    }

    public void toggle() {
        if (player == null) {
            if (currentIndex >= 0 && currentIndex < queue.size()) {
                play(queue.get(currentIndex));
            }
            return;
        }
        if (player.isPlaying()) {
            player.pause();
            isPlaying.setValue(false);
        } else {
            player.start();
            isPlaying.setValue(true);
            mainHandler.post(progressTicker);
        }
    }

    public void next() {
        if (queue.isEmpty()) return;
        currentIndex = (currentIndex + 1) % queue.size();
        play(queue.get(currentIndex));
    }

    public void previous() {
        if (queue.isEmpty()) return;
        if (player != null && player.getCurrentPosition() > 3000) {
            player.seekTo(0);
            return;
        }
        currentIndex = (currentIndex - 1 + queue.size()) % queue.size();
        play(queue.get(currentIndex));
    }

    public void seekTo(int ms) {
        if (player != null) {
            try { player.seekTo(ms); } catch (IllegalStateException ignored) {}
        }
    }

    public void stop() {
        if (player != null) {
            try { player.stop(); } catch (IllegalStateException ignored) {}
            player.release();
            player = null;
        }
        isPlaying.setValue(false);
        progressMs.setValue(0);
        durationMs.setValue(0);
    }

    @Nullable
    public Track getCurrent() {
        return currentTrack.getValue();
    }

    private void play(Track t) {
        try {
            if (player != null) {
                player.reset();
            } else {
                player = new MediaPlayer();
                player.setAudioAttributes(new AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_MEDIA)
                        .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                        .build());
            }
            player.setDataSource(appContext, Uri.parse(t.filePath));
            player.setOnPreparedListener(mp -> {
                durationMs.setValue(mp.getDuration());
                mp.start();
                isPlaying.setValue(true);
                mainHandler.post(progressTicker);
            });
            player.setOnCompletionListener(mp -> next());
            player.setOnErrorListener((mp, what, extra) -> {
                isPlaying.setValue(false);
                return true;
            });
            currentTrack.setValue(t);
            progressMs.setValue(0);
            player.prepareAsync();
        } catch (Exception e) {
            isPlaying.setValue(false);
        }
    }
}
