package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Playlist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, Long> {

    boolean existsByNameIgnoreCaseAndUser_id(String name, Long userId);

    Optional<Playlist> findByNameIgnoreCaseAndUser_id(String name, Long userId);

    List<Playlist> findAllByUser_Id(Long userId);

    List<Playlist> findByisPublicTrueAndNameContainingIgnoreCase(String name);

    List<Playlist> findByUser_IdAndNameContainingIgnoreCase(Long userId, String name);


}