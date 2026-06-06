package com.everest.soundwave.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.everest.soundwave.R;
import com.everest.soundwave.data.PlaylistRepository;
import com.everest.soundwave.data.TrackRepository;

public class AddTracksFragment extends Fragment {

    private static final String ARG_PLAYLIST_ID = "playlistId";

    public static AddTracksFragment create(long playlistId) {
        AddTracksFragment f = new AddTracksFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_PLAYLIST_ID, playlistId);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_add_tracks, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long playlistId = requireArguments().getLong(ARG_PLAYLIST_ID);

        view.findViewById(R.id.at_back).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        RecyclerView rv = view.findViewById(R.id.rv_add_tracks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));

        TrackRowAdapter adapter = new TrackRowAdapter((t, pos, list) -> {
            PlaylistRepository.get(requireContext()).addTrack(playlistId, t.id);
            Toast.makeText(requireContext(), R.string.track_added, Toast.LENGTH_SHORT).show();
        });
        rv.setAdapter(adapter);

        TrackRepository.get(requireContext()).observeAll()
                .observe(getViewLifecycleOwner(), adapter::submit);
    }
}
