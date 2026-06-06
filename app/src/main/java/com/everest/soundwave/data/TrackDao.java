package com.everest.soundwave.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface TrackDao {

    @Insert
    long insert(Track track);

    @Delete
    void delete(Track track);

    @Query("SELECT * FROM tracks ORDER BY addedAt DESC")
    LiveData<List<Track>> observeAll();

    @Query("SELECT * FROM tracks ORDER BY addedAt DESC")
    List<Track> getAllSync();

    @Query("SELECT * FROM tracks ORDER BY addedAt DESC LIMIT :limit")
    LiveData<List<Track>> observeRecent(int limit);

    @Query("SELECT * FROM tracks WHERE title LIKE '%' || :q || '%' OR artist LIKE '%' || :q || '%' ORDER BY addedAt DESC")
    LiveData<List<Track>> search(String q);

    @Query("SELECT * FROM tracks WHERE id = :id LIMIT 1")
    Track getById(long id);

    @Query("SELECT * FROM tracks WHERE id IN (:ids)")
    LiveData<List<Track>> getByIds(List<Long> ids);

    @Query("SELECT * FROM tracks WHERE isLiked = 1 ORDER BY addedAt DESC")
    LiveData<List<Track>> observeLiked();

    @Query("SELECT COUNT(*) FROM tracks WHERE isLiked = 1")
    LiveData<Integer> observeLikedCount();

    @Query("UPDATE tracks SET isLiked = :liked WHERE id = :id")
    void setLiked(long id, boolean liked);

    @Query("UPDATE tracks SET title = :title WHERE id = :id")
    void rename(long id, String title);

    @Query("SELECT * FROM tracks WHERE remoteId = :remoteId LIMIT 1")
    Track findByRemoteId(String remoteId);
}
