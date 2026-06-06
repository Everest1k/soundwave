package com.everest.soundwave.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.content.Intent;

import com.everest.soundwave.LoginActivity;
import com.everest.soundwave.R;
import com.everest.soundwave.auth.AuthManager;
import com.everest.soundwave.data.PlaylistRepository;
import com.everest.soundwave.data.TrackRepository;
import com.everest.soundwave.player.PlayerManager;

public class LibraryFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_library, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView favCount = view.findViewById(R.id.fav_count);
        TextView plCount = view.findViewById(R.id.pl_count);

        TrackRepository.get(requireContext()).observeLikedCount()
                .observe(getViewLifecycleOwner(), n -> {
                    int count = n == null ? 0 : n;
                    favCount.setText(formatTracks(count));
                });

        PlaylistRepository.get(requireContext()).observeCount()
                .observe(getViewLifecycleOwner(), n -> {
                    int count = n == null ? 0 : n;
                    plCount.setText(formatPlaylists(count));
                });

        view.findViewById(R.id.section_favorites).setOnClickListener(v -> push(new FavoritesFragment()));
        view.findViewById(R.id.section_playlists).setOnClickListener(v -> push(new PlaylistsFragment()));

        AuthManager auth = AuthManager.get(requireContext());
        TextView userLabel = view.findViewById(R.id.lib_user_label);
        String roleLabel = auth.isAdmin() ? "Администратор" : "Пользователь";
        userLabel.setText(roleLabel + ": " + auth.currentUserName());

        view.findViewById(R.id.lib_logout).setOnClickListener(v -> {
            PlayerManager.get(requireContext()).stop();
            auth.logout();
            Intent i = new Intent(requireContext(), LoginActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
            requireActivity().finish();
        });
    }

    private void push(Fragment f) {
        getParentFragmentManager().beginTransaction()
                .hide(this)
                .add(R.id.fragment_container, f)
                .addToBackStack(null)
                .commit();
    }

    private static String formatTracks(int n) {
        int m = n % 100, r = n % 10;
        if (m >= 11 && m <= 19) return n + " треков";
        if (r == 1) return n + " трек";
        if (r >= 2 && r <= 4) return n + " трека";
        return n + " треков";
    }

    private static String formatPlaylists(int n) {
        int m = n % 100, r = n % 10;
        if (m >= 11 && m <= 19) return n + " плейлистов";
        if (r == 1) return n + " плейлист";
        if (r >= 2 && r <= 4) return n + " плейлиста";
        return n + " плейлистов";
    }
}
