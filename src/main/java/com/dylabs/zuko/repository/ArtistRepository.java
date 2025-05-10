package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Artist;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;


public interface ArtistRepository extends JpaRepository<Artist, Long> {

    Optional<Artist> findByName(String name);
    Optional<Artist> findByUserId(Long userId);

}