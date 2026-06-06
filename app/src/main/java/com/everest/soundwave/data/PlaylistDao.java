package com.everest.soundwave.data;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface PlaylistDao {

    @Insert
    long insert(Playlist p);

    @Delete
    void delete(Playlist p);

    @Query("SELECT p.*, (SELECT COUNT(*) FROM playlist_tracks WHERE playlistId = p.id) AS trackCount " +
           "FROM playlists p WHERE p.ownerId = :ownerId ORDER BY p.createdAt DESC")
    LiveData<List<PlaylistWithCount>> observeAllForOwner(String ownerId);

    @Query("SELECT COUNT(*) FROM playlists WHERE ownerId = :ownerId")
    LiveData<Integer> observeCountForOwner(String ownerId);

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insertTrack(PlaylistTrack pt);

    @Delete
    void deleteTrack(PlaylistTrack pt);

    @Query("SELECT t.* FROM tracks t INNER JOIN playlist_tracks pt ON t.id = pt.trackId " +
           "WHERE pt.playlistId = :playlistId ORDER BY pt.addedAt ASC")
    LiveData<List<Track>> observeTracks(long playlistId);

    @Query("DELETE FROM playlist_tracks WHERE trackId = :trackId")
    void removeTrackFromAllPlaylists(long trackId);
}
