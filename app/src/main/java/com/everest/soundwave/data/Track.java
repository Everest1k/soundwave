package com.everest.soundwave.data;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "tracks")
public class Track {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String title = "";

    @NonNull
    public String artist = "";

    @NonNull
    public String filePath = "";

    @Nullable
    public String coverPath;

    public long durationMs;

    public long addedAt;

    public boolean isLiked = false;

    @Nullable
    public String remoteId;

    public Track() {}

    public Track(@NonNull String title,
                 @NonNull String artist,
                 @NonNull String filePath,
                 @Nullable String coverPath,
                 long durationMs,
                 long addedAt) {
        this.title = title;
        this.artist = artist;
        this.filePath = filePath;
        this.coverPath = coverPath;
        this.durationMs = durationMs;
        this.addedAt = addedAt;
    }
}
