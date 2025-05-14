package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    // Verificar si una playlist existe por nombre y usuario
    boolean existsByNameIgnoreCaseAndUsers_UserId(String name, Long userId);

    // Buscar una playlist por ID y usuario asociado
    Optional<Playlist> findByPlaylistIdAndUsers_UserId(Long playlistId, Long userId);

    // Buscar todas las playlists públicas
    List<Playlist> findAllByIsPublicTrue();
}