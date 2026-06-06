package com.everest.soundwave;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.everest.soundwave.data.Track;
import com.everest.soundwave.data.TrackRepository;
import com.everest.soundwave.player.PlayerManager;
import com.everest.soundwave.ui.CoverLoader;

import java.util.Locale;

public class PlayerActivity extends AppCompatActivity {

    private ImageView cover;
    private TextView headerTitle, title, artist, timeCurrent, timeRemaining;
    private SeekBar seek;
    private ImageButton playBtn, likeBtn;

    private boolean userSeeking = false;
    private Track currentTrack;
    private boolean isLiked = false;
    private androidx.lifecycle.LiveData<Boolean> likedLive;
    private androidx.lifecycle.Observer<Boolean> likedObserver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        cover = findViewById(R.id.p_cover);
        headerTitle = findViewById(R.id.p_header_title);
        title = findViewById(R.id.p_title);
        artist = findViewById(R.id.p_artist);
        timeCurrent = findViewById(R.id.p_time_current);
        timeRemaining = findViewById(R.id.p_time_total);
        seek = findViewById(R.id.p_seek);
        playBtn = findViewById(R.id.p_play);
        likeBtn = findViewById(R.id.p_like);

        ImageButton prevBtn = findViewById(R.id.p_prev);
        ImageButton nextBtn = findViewById(R.id.p_next);
        ImageButton backBtn = findViewById(R.id.p_back);

        PlayerManager pm = PlayerManager.get(this);

        pm.currentTrack().observe(this, this::bindTrack);
        pm.isPlaying().observe(this, playing ->
                playBtn.setImageResource(Boolean.TRUE.equals(playing)
                        ? R.drawable.ic_pause : R.drawable.ic_play));

        pm.durationMs().observe(this, ms -> {
            int total = ms == null ? 0 : ms;
            if (total > 0) timeRemaining.setText("-" + formatTime(total));
        });

        pm.progressMs().observe(this, ms -> {
            if (userSeeking) return;
            int pos = ms == null ? 0 : ms;
            timeCurrent.setText(formatTime(pos));
            Integer total = pm.durationMs().getValue();
            if (total != null && total > 0) {
                seek.setProgress((int) ((pos / (float) total) * 1000));
                timeRemaining.setText("-" + formatTime(total - pos));
            }
        });

        playBtn.setOnClickListener(v -> pm.toggle());
        prevBtn.setOnClickListener(v -> pm.previous());
        nextBtn.setOnClickListener(v -> pm.next());
        backBtn.setOnClickListener(v -> finish());

        likeBtn.setOnClickListener(v -> {
            if (currentTrack == null) return;
            boolean nowLiked = !isLiked;
            TrackRepository.get(this).setLiked(currentTrack.id, nowLiked);
        });

        seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar bar, int progress, boolean fromUser) {}
            @Override public void onStartTrackingTouch(SeekBar bar) { userSeeking = true; }
            @Override public void onStopTrackingTouch(SeekBar bar) {
                userSeeking = false;
                Integer total = pm.durationMs().getValue();
                if (total != null && total > 0) {
                    pm.seekTo((int) ((bar.getProgress() / 1000f) * total));
                }
            }
        });
    }

    private void bindTrack(Track t) {
        if (t == null) { finish(); return; }
        currentTrack = t;
        headerTitle.setText(t.title);
        title.setText(t.title);
        artist.setText(t.artist == null || t.artist.isEmpty()
                ? getString(R.string.unknown_artist) : t.artist);
        CoverLoader.into(cover, t.coverPath);

        if (likedLive != null && likedObserver != null) {
            likedLive.removeObserver(likedObserver);
        }
        likedLive = TrackRepository.get(this).observeLiked(t.id);
        likedObserver = liked -> {
            isLiked = Boolean.TRUE.equals(liked);
            updateLikeColor(isLiked);
        };
        likedLive.observe(this, likedObserver);
    }

    private void updateLikeColor(boolean liked) {
        likeBtn.setColorFilter(liked
                ? getResources().getColor(R.color.color_play_button, getTheme())
                : getResources().getColor(R.color.text_primary, getTheme()));
    }

    private static String formatTime(int ms) {
        int total = ms / 1000;
        int m = total / 60;
        int s = total % 60;
        return String.format(Locale.US, "%d:%02d", m, s);
    }
}
