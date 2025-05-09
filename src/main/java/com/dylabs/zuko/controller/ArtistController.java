package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.service.ArtistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("artists") // No tiene el "/" al principio
@RequiredArgsConstructor

public class ArtistController {

    private final ArtistService artistService;

    // Crear un artista
    @PostMapping
    public ResponseEntity<ArtistResponse> createArtist(@RequestBody @Valid CreateArtistRequest request) {
        ArtistResponse response = artistService.createArtist(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    //  más métodos para obtener artistas, actualizar, eliminar, etc.
}
