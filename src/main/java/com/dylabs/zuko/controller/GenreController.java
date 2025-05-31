package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.ApiResponse;
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
@RequestMapping("genres")
@RequiredArgsConstructor
public class GenreController {
    private final GenreService genreService;

    @PostMapping
    public ResponseEntity<ApiResponse<GenreResponse>> createGenre(@Valid @RequestBody GenreRequest request) {
        GenreResponse response = genreService.createGenre(request);
        return ResponseEntity
                .status(HttpStatus.CREATED).
                body(new ApiResponse<>("Género registrado exitosamente.", response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<GenreResponse>>> getAllGenres() {
        List<GenreResponse> genres = genreService.getGenres();
        return ResponseEntity
                .ok(new ApiResponse<>("Lista de géneros obtenida exitosamente.",genres));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GenreResponse>> updateGenre(@PathVariable long id, @Valid @RequestBody GenreRequest request) {
        GenreResponse updatedGenre = genreService.updateGenre(id, request);
        return ResponseEntity
                .ok(new ApiResponse<>("Género actualizado correctamente.", updatedGenre));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGenre(@PathVariable long id) {
        genreService.deleteGenre(id);
        return ResponseEntity
                .ok(new ApiResponse<>("Género eliminado correctamente.",null));
    }


}
