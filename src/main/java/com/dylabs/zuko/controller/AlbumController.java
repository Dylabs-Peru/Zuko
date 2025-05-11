package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<AlbumResponse> createAlbum(@RequestBody @Valid AlbumRequest request) {
        AlbumResponse response = albumService.createAlbum(request);
        return ResponseEntity.ok(response);
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

}
