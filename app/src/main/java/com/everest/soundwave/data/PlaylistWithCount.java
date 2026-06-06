package com.everest.soundwave.data;

import androidx.room.ColumnInfo;
import androidx.room.Embedded;

public class PlaylistWithCount {

    @Embedded
    public Playlist playlist;

    @ColumnInfo(name = "trackCount")
    public int trackCount;
}
