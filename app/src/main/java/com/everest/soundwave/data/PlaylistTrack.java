package com.everest.soundwave.data;

import androidx.room.Entity;

@Entity(tableName = "playlist_tracks", primaryKeys = {"playlistId", "trackId"})
public class PlaylistTrack {
    public long playlistId;
    public long trackId;
    public long addedAt;
}
