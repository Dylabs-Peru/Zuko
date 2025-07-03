package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SongRepository extends JpaRepository<Song, Long> {
    boolean existsByTitleIgnoreCase(String title);
    boolean existsByTitleIgnoreCaseAndArtistId(String title, Long artistId);
    List<Song> findByTitleContainingIgnoreCaseAndIsPublicSongTrue(String title);
    List<Song> findAllByArtistId(Long artistId);
    List<Song> findAll();
    List<Song> findTop3ByIsPublicSongTrueOrderByIdDesc();
}