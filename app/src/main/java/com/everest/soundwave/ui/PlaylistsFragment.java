package com.everest.soundwave.ui;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.everest.soundwave.R;
import com.everest.soundwave.data.PlaylistRepository;

public class PlaylistsFragment extends Fragment {

    private PlaylistsAdapter adapter;
    private TextView empty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_playlists, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        view.findViewById(R.id.pl_back).setOnClickListener(v ->
                getParentFragmentManager().popBackStack());

        empty = view.findViewById(R.id.pl_empty);

        RecyclerView rv = view.findViewById(R.id.rv_playlists);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new PlaylistsAdapter(pwc -> {
            PlaylistDetailFragment detail = PlaylistDetailFragment.create(
                    pwc.playlist.id, pwc.playlist.name);
            getParentFragmentManager().beginTransaction()
                    .hide(this)
                    .add(R.id.fragment_container, detail)
                    .addToBackStack(null)
                    .commit();
        });
        rv.setAdapter(adapter);

        PlaylistRepository.get(requireContext()).observeAll()
                .observe(getViewLifecycleOwner(), list -> {
                    adapter.submit(list);
                    empty.setVisibility(list == null || list.isEmpty() ? View.VISIBLE : View.GONE);
                });

        view.findViewById(R.id.btn_new_playlist).setOnClickListener(v -> showCreateDialog());
    }

    private void showCreateDialog() {
        EditText et = new EditText(requireContext());
        et.setHint(getString(R.string.playlist_name_hint));
        et.setSingleLine(true);
        int pad = (int) (16 * getResources().getDisplayMetrics().density);
        et.setPadding(pad, pad, pad, pad);

        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.new_playlist)
                .setView(et)
                .setPositiveButton(R.string.create, (d, w) -> {
                    String name = et.getText().toString().trim();
                    if (!name.isEmpty()) PlaylistRepository.get(requireContext()).create(name);
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
