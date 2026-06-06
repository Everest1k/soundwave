package com.everest.soundwave.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.everest.soundwave.R;
import com.everest.soundwave.data.PlaylistWithCount;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsAdapter extends RecyclerView.Adapter<PlaylistsAdapter.VH> {

    public interface OnClick { void onClick(PlaylistWithCount p); }

    private List<PlaylistWithCount> items = new ArrayList<>();
    private final OnClick listener;

    public PlaylistsAdapter(OnClick listener) {
        this.listener = listener;
    }

    public void submit(List<PlaylistWithCount> list) {
        items = list != null ? list : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_library_section, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int pos) {
        PlaylistWithCount pwc = items.get(pos);
        h.title.setText(pwc.playlist.name);
        h.subtitle.setText(formatTracks(pwc.trackCount));
        h.cover.setImageResource(R.drawable.cover_placeholder);
        h.itemView.setOnClickListener(v -> { if (listener != null) listener.onClick(pwc); });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title, subtitle;
        VH(@NonNull View v) {
            super(v);
            cover = v.findViewById(R.id.section_cover);
            title = v.findViewById(R.id.section_title);
            subtitle = v.findViewById(R.id.section_subtitle);
        }
    }

    private static String formatTracks(int n) {
        int m = n % 100, r = n % 10;
        if (m >= 11 && m <= 19) return n + " треков";
        if (r == 1) return n + " трек";
        if (r >= 2 && r <= 4) return n + " трека";
        return n + " треков";
    }
}
