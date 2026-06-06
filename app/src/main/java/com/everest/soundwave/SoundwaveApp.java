package com.everest.soundwave;

import android.app.Application;

import com.everest.soundwave.data.TrackRepository;
import com.everest.soundwave.player.PlayerManager;

public class SoundwaveApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        TrackRepository.get(this);
        PlayerManager.get(this);
    }
}
