package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Song;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SongRepository extends JpaRepository<Song, Long> {
    boolean existsByTitleIgnoreCase(String title);

    // Método para obtener todas las canciones de un artista por su ID
    List<Song> findAllByArtistId(Long artistId);
}
