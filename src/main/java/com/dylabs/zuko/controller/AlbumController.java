package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<Object> createAlbum(@RequestBody @Valid AlbumRequest request) {
        AlbumResponse response = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Álbum creado correctamente",
                        "data", response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAlbumById(@PathVariable Long id) {
        AlbumResponse response = albumService.getAlbumById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum obtenido correctamente",
                        "data", response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAlbum(@PathVariable Long id, @RequestBody @Valid AlbumRequest request) {
        AlbumResponse response = albumService.updateAlbum(id, request);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum actualizado correctamente",
                        "data", response
                )
        );
    }

    // Manejo de excepciones específicas

    @ExceptionHandler(AlbumAlreadyExistsException.class)
    public ResponseEntity<String> handleAlbumAlreadyExists(AlbumAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(ArtistNotFoundException.class)
    public ResponseEntity<String> handleArtistNotFound(ArtistNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(GenreNotFoundException.class)
    public ResponseEntity<String> handleGenreNotFound(GenreNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(AlbumNotFoundException.class)
    public ResponseEntity<String> handleAlbumNotFound(AlbumNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
