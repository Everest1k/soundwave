package com.everest.soundwave;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.everest.soundwave.auth.AuthManager;
import com.everest.soundwave.ui.CoverLoader;
import com.everest.soundwave.data.Track;
import com.everest.soundwave.player.PlayerManager;
import com.everest.soundwave.remote.RemoteTracksConfig;
import com.everest.soundwave.remote.YandexDiskLoader;
import com.everest.soundwave.ui.HomeFragment;
import com.everest.soundwave.ui.LibraryFragment;
import com.everest.soundwave.ui.SearchFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private View miniPlayer;
    private ImageView mpCover;
    private TextView mpTitle, mpArtist;
    private ImageButton mpPlay;
    private ProgressBar mpProgress;

    private static final String TAG_HOME = "home";
    private static final String TAG_SEARCH = "search";
    private static final String TAG_LIBRARY = "library";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (!AuthManager.get(this).isLoggedIn()) {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return;
        }

        setContentView(R.layout.activity_main);

        if (savedInstanceState == null && RemoteTracksConfig.isConfigured()) {
            YandexDiskLoader.sync(this, (added, err) -> {});
        }

        miniPlayer = findViewById(R.id.mini_player);
        mpCover = miniPlayer.findViewById(R.id.mp_cover);
        mpTitle = miniPlayer.findViewById(R.id.mp_title);
        mpArtist = miniPlayer.findViewById(R.id.mp_artist);
        mpPlay = miniPlayer.findViewById(R.id.mp_play);
        mpProgress = miniPlayer.findViewById(R.id.mp_progress);

        BottomNavigationView nav = findViewById(R.id.bottom_nav);
        nav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) {
                showFragment(TAG_HOME);
                return true;
            } else if (id == R.id.nav_search) {
                showFragment(TAG_SEARCH);
                return true;
            } else if (id == R.id.nav_library) {
                showFragment(TAG_LIBRARY);
                return true;
            }
            return false;
        });

        if (savedInstanceState == null) {
            showFragment(TAG_HOME);
        }

        PlayerManager pm = PlayerManager.get(this);

        pm.currentTrack().observe(this, this::bindMiniPlayerTrack);
        pm.isPlaying().observe(this, playing ->
                mpPlay.setImageResource(Boolean.TRUE.equals(playing)
                        ? R.drawable.ic_pause : R.drawable.ic_play));
        pm.progressMs().observe(this, ms -> {
            Integer total = pm.durationMs().getValue();
            if (total != null && total > 0 && ms != null) {
                mpProgress.setProgress((int) ((ms / (float) total) * 1000));
            }
        });

        mpPlay.setOnClickListener(v -> pm.toggle());
        miniPlayer.setOnClickListener(v -> {
            Track t = pm.getCurrent();
            if (t != null) startActivity(new Intent(this, PlayerActivity.class));
        });
    }

    private void bindMiniPlayerTrack(Track t) {
        if (t == null) {
            miniPlayer.setVisibility(View.GONE);
            return;
        }
        miniPlayer.setVisibility(View.VISIBLE);
        mpTitle.setText(t.title);
        mpArtist.setText(t.artist == null || t.artist.isEmpty()
                ? getString(R.string.unknown_artist) : t.artist);
        CoverLoader.into(mpCover, t.coverPath);
    }

    private void showFragment(String tag) {
        FragmentManager fm = getSupportFragmentManager();
        Fragment current = null;
        for (Fragment f : fm.getFragments()) {
            if (f.isVisible()) current = f;
        }
        Fragment target = fm.findFragmentByTag(tag);
        if (target == null) {
            switch (tag) {
                case TAG_SEARCH: target = new SearchFragment(); break;
                case TAG_LIBRARY: target = new LibraryFragment(); break;
                default: target = new HomeFragment(); break;
            }
        }
        if (target == current) return;

        androidx.fragment.app.FragmentTransaction tx = fm.beginTransaction();
        if (current != null) tx.hide(current);
        if (target.isAdded()) {
            tx.show(target);
        } else {
            tx.add(R.id.fragment_container, target, tag);
        }
        tx.commit();
    }
}
