package com.everest.soundwave.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.everest.soundwave.R;
import com.everest.soundwave.auth.AuthManager;
import com.everest.soundwave.data.TrackRepository;
import com.everest.soundwave.player.PlayerManager;
import com.everest.soundwave.remote.YandexDiskLoader;

public class HomeFragment extends Fragment {

    private TrackCardAdapter adapter;
    private TextView emptyHint;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_home, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        emptyHint = view.findViewById(R.id.empty_recommendations);

        RecyclerView rv = view.findViewById(R.id.rv_recommendations);
        rv.setLayoutManager(new StaggeredGridLayoutManager(
                2, StaggeredGridLayoutManager.HORIZONTAL));
        boolean isAdmin = AuthManager.get(requireContext()).isAdmin();
        adapter = new TrackCardAdapter((t, position, list) -> {
            PlayerManager.get(requireContext()).setQueue(list, position);
        });
        rv.setAdapter(adapter);

        TrackRepository.get(requireContext()).observeRecent(20).observe(getViewLifecycleOwner(), tracks -> {
            adapter.submit(tracks);
            emptyHint.setVisibility(tracks == null || tracks.isEmpty() ? View.VISIBLE : View.GONE);
        });

        Button btnRefresh = view.findViewById(R.id.btn_refresh_tracks);
        btnRefresh.setOnClickListener(v -> {
            btnRefresh.setEnabled(false);
            Toast.makeText(requireContext(), R.string.refresh_in_progress, Toast.LENGTH_SHORT).show();
            YandexDiskLoader.sync(requireContext(), (added, error) -> {
                if (!isAdded()) return;
                btnRefresh.setEnabled(true);
                if (error != null) {
                    Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(requireContext(),
                            getString(R.string.refresh_added, added),
                            Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
