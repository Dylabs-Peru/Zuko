package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Genre;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GenreRepository extends JpaRepository<Genre,Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Genre> findByNameIgnoreCase(String name);
    Optional<Genre> findById(long id);
    void deleteById(long id);
}
