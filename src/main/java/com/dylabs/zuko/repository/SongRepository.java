package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndArtistId(String title, Long artistId);
    List<Song> findByTitleContainingIgnoreCaseAndIsPublicSongTrue(String title);
    List<Song> findAllByArtistId(Long artistId);
}