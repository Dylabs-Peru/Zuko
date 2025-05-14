package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    boolean existsByNameIgnoreCaseAndUsers_id(String name, Long usersId);

    Optional<Playlist> findByPlaylistIdAndUsers_id(Long playlistId, Long usersId);
    

    // Buscar todas las playlists públicas
    List<Playlist> findAllByIsPublicTrue();
}