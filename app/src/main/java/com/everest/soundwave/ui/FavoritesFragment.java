package com.everest.soundwave.ui;

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
import com.everest.soundwave.data.TrackRepository;
import com.everest.soundwave.player.PlayerManager;

public class FavoritesFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_favorites, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.fav_back).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        TextView empty = view.findViewById(R.id.fav_empty);

        RecyclerView rv = view.findViewById(R.id.rv_favorites);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        TrackRowAdapter adapter = new TrackRowAdapter((t, pos, list) ->
                PlayerManager.get(requireContext()).setQueue(list, pos));
        rv.setAdapter(adapter);

        TrackRepository.get(requireContext()).observeLiked()
                .observe(getViewLifecycleOwner(), tracks -> {
                    adapter.submit(tracks);
                    empty.setVisibility(tracks == null || tracks.isEmpty() ? View.VISIBLE : View.GONE);
                });
    }
}
