package com.everest.soundwave.ui;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.everest.soundwave.R;
import com.everest.soundwave.data.RecentTracksPrefs;
import com.everest.soundwave.data.Track;
import com.everest.soundwave.data.TrackRepository;
import com.everest.soundwave.player.PlayerManager;

import java.util.List;

public class SearchFragment extends Fragment {

    private TrackRowAdapter adapter;
    private LiveData<List<Track>> currentSource;
    private final Observer<List<Track>> observer = tracks -> adapter.submit(tracks);
    private View recentHeader;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.rv_search);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TrackRowAdapter((t, position, list) -> {
            RecentTracksPrefs.add(requireContext(), t.id);
            PlayerManager.get(requireContext()).setQueue(list, position);
        });
        rv.setAdapter(adapter);

        recentHeader = view.findViewById(R.id.recent_header);

        TextView btnClear = view.findViewById(R.id.btn_clear_recent);
        btnClear.setOnClickListener(v -> {
            RecentTracksPrefs.clear(requireContext());
            bindSearch("");
        });

        EditText input = view.findViewById(R.id.search_input);
        input.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                bindSearch(s == null ? "" : s.toString().trim());
            }
            @Override public void afterTextChanged(Editable s) {}
        });

        bindSearch("");
    }

    private void bindSearch(String q) {
        if (currentSource != null) currentSource.removeObserver(observer);

        if (q.isEmpty()) {
            recentHeader.setVisibility(View.VISIBLE);
            List<Long> ids = RecentTracksPrefs.getIds(requireContext());
            currentSource = TrackRepository.get(requireContext()).observeByIds(ids);
        } else {
            recentHeader.setVisibility(View.GONE);
            currentSource = TrackRepository.get(requireContext()).search(q);
        }
        currentSource.observe(getViewLifecycleOwner(), observer);
    }
}
