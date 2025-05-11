package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SongRepository extends JpaRepository<Song, Long> {
    boolean existsByTitleIgnoreCase(String title);
}