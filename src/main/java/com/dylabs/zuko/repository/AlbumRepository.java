package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Song;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
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

    @Query("SELECT a FROM Album a WHERE a.releaseDate = CURRENT_DATE ORDER BY a.releaseDate DESC, a.id DESC")
    Page<Album> findTopByReleaseDateToday(Pageable pageable);
}
