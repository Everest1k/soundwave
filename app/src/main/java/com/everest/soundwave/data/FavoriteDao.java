package com.everest.soundwave.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface FavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void add(Favorite f);

    @Query("DELETE FROM favorites WHERE userId = :userId AND trackId = :trackId")
    void remove(String userId, long trackId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND trackId = :trackId)")
    boolean isLiked(String userId, long trackId);

    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE userId = :userId AND trackId = :trackId)")
    LiveData<Boolean> observeLiked(String userId, long trackId);

    @Query("SELECT t.* FROM tracks t INNER JOIN favorites f ON t.id = f.trackId " +
            "WHERE f.userId = :userId ORDER BY f.addedAt DESC")
    LiveData<List<Track>> observeUserLiked(String userId);

    @Query("SELECT COUNT(*) FROM favorites WHERE userId = :userId")
    LiveData<Integer> observeUserLikedCount(String userId);

    @Query("DELETE FROM favorites WHERE trackId = :trackId")
    void deleteForTrack(long trackId);
}
