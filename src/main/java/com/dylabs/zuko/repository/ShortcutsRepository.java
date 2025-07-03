package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Shortcuts;
import org.springframework.data.domain.Limit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShortcutsRepository extends JpaRepository<Shortcuts, Long> {
    Optional<Shortcuts> findByUser_Id(Long userId);
    boolean existsByUser_IdAndPlaylists_PlaylistId(Long userId, Long playlistId);
    boolean existsByUser_IdAndAlbums_Id(Long userId, Long albumId);
}
