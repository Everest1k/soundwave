package com.everest.soundwave.ui;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.everest.soundwave.R;
import com.everest.soundwave.auth.AuthManager;
import com.everest.soundwave.data.Track;

import java.util.ArrayList;
import java.util.List;

public class TrackRowAdapter extends RecyclerView.Adapter<TrackRowAdapter.VH> {

    public interface OnTrackClick { void onClick(Track t, int position, List<Track> list); }

    private final List<Track> items = new ArrayList<>();
    private final OnTrackClick listener;

    public TrackRowAdapter(OnTrackClick listener) {
        this.listener = listener;
    }

    public void submit(List<Track> tracks) {
        items.clear();
        if (tracks != null) items.addAll(tracks);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_track_row, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Track t = items.get(position);
        h.title.setText(t.title);
        h.artist.setText(t.artist == null || t.artist.isEmpty()
                ? h.itemView.getContext().getString(R.string.unknown_artist) : t.artist);
        CoverLoader.into(h.cover, t.coverPath);
        h.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onClick(t, h.getAdapterPosition(), items);
        });
        h.itemView.setOnLongClickListener(v -> {
            Context ctx = v.getContext();
            if (!AuthManager.get(ctx).isAdmin()) return false;
            AdminTrackActions.show(v, t);
            return true;
        });
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class VH extends RecyclerView.ViewHolder {
        final ImageView cover;
        final TextView title, artist;
        VH(@NonNull View v) {
            super(v);
            cover = v.findViewById(R.id.row_cover);
            title = v.findViewById(R.id.row_title);
            artist = v.findViewById(R.id.row_artist);
        }
    }
}
