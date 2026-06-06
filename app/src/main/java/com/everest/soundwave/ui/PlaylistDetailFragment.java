package com.everest.soundwave.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.everest.soundwave.R;
import com.everest.soundwave.data.Playlist;
import com.everest.soundwave.data.PlaylistRepository;
import com.everest.soundwave.player.PlayerManager;

public class PlaylistDetailFragment extends Fragment {

    private static final String ARG_ID = "playlistId";
    private static final String ARG_NAME = "playlistName";

    public static PlaylistDetailFragment create(long id, String name) {
        PlaylistDetailFragment f = new PlaylistDetailFragment();
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        args.putString(ARG_NAME, name);
        f.setArguments(args);
        return f;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlist_detail, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        long playlistId = requireArguments().getLong(ARG_ID);
        String playlistName = requireArguments().getString(ARG_NAME, "");

        ((TextView) view.findViewById(R.id.pd_title)).setText(playlistName);

        view.findViewById(R.id.pd_back).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        view.findViewById(R.id.pd_delete).setOnClickListener(v ->
                new AlertDialog.Builder(requireContext())
                        .setTitle(R.string.delete_confirm)
                        .setMessage(playlistName)
                        .setPositiveButton(R.string.delete, (d, w) -> {
                            Playlist p = new Playlist();
                            p.id = playlistId;
                            p.name = playlistName;
                            PlaylistRepository.get(requireContext()).delete(p);
                            getParentFragmentManager().popBackStack();
                        })
                        .setNegativeButton(R.string.cancel, null)
                        .show());

        TextView empty = view.findViewById(R.id.pd_empty);

        RecyclerView rv = view.findViewById(R.id.rv_playlist_tracks);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        TrackRowAdapter adapter = new TrackRowAdapter((t, pos, list) ->
                PlayerManager.get(requireContext()).setQueue(list, pos));
        rv.setAdapter(adapter);

        PlaylistRepository.get(requireContext()).observeTracks(playlistId)
                .observe(getViewLifecycleOwner(), tracks -> {
                    adapter.submit(tracks);
                    empty.setVisibility(tracks == null || tracks.isEmpty() ? View.VISIBLE : View.GONE);
                });

        view.findViewById(R.id.pd_add).setOnClickListener(v -> {
            AddTracksFragment addFrag = AddTracksFragment.create(playlistId);
            getParentFragmentManager().beginTransaction()
                    .hide(this)
                    .add(R.id.fragment_container, addFrag)
                    .addToBackStack(null)
                    .commit();
        });
    }
}
