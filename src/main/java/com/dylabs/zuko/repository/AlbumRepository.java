package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Album;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AlbumRepository extends JpaRepository<Album, Long> {
    boolean existsByTitleIgnoreCaseAndArtistId(String title, Long artistId);

    boolean existsByTitleIgnoreCaseAndArtistIdAndIdNot(String title, Long artistId, Long id);

    boolean existsByGenreId(Long genreId);

    List<Album> findAllByTitleContainingIgnoreCase(String title);
    List<Album> findAllByTitleContainingIgnoreCaseAndArtistId(String title, Long artistId);
    List<Album> findAllByOrderByTitleAsc();
    @Query("SELECT a FROM Album a JOIN a.songs s WHERE s.id =:songId")
    Optional<Album> findAlbumBySongId(@Param("songId") Long songId);
    List<Album> findAllByArtistId(Long artistId);
}
