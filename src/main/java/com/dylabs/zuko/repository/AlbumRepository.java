package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    boolean existsByTitleIgnoreCaseAndArtistId(String title, Long artistId);

    boolean existsByTitleIgnoreCaseAndArtistIdAndIdNot(String title, Long artistId, Long id);
}
