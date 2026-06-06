package com.everest.soundwave.data;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "favorites", primaryKeys = {"userId", "trackId"})
public class Favorite {

    @NonNull
    public String userId = "";

    public long trackId;

    public long addedAt;
}
