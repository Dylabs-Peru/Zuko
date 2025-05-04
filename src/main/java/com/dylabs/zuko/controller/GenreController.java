package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.GenreRequest;
import com.dylabs.zuko.dto.response.GenreResponse;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.service.GenreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<GenreResponse> createGenre(@Valid @RequestBody GenreRequest request) {
        GenreResponse response = genreService.createGenre(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<GenreResponse>> getAllGenres() {
        List<GenreResponse> genres = genreService.getGenres();
        return new ResponseEntity<>(genres, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<GenreResponse> updateGenre(@PathVariable long id, @Valid @RequestBody GenreRequest request) {
        GenreResponse updatedGenre = genreService.updateGenre(id, request);
        return new ResponseEntity<>(updatedGenre, HttpStatus.OK);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteGenre(@PathVariable long id) {
        boolean deleted = genreService.deleteGenre(id);
        return deleted
                ? ResponseEntity.ok().build()
                : ResponseEntity.notFound().build();
    }

}
