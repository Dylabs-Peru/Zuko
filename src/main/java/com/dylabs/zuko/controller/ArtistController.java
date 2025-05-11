package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.service.ArtistService;
import com.dylabs.zuko.dto.request.UpdateArtistRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("artists")
@RequiredArgsConstructor

public class ArtistController {

    private final ArtistService artistService;

    @PostMapping("/{username}")
    public ResponseEntity<ApiResponse<ArtistResponse>> createArtist(
            @PathVariable String username,
            @Valid @RequestBody CreateArtistRequest request
    ) {
        ArtistResponse response = artistService.createArtist(request, username);
        return new ResponseEntity<>(new ApiResponse<>("Artista creado exitosamente", response), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ArtistResponse> updateArtist(
            @PathVariable Long id,
            @Valid @RequestBody UpdateArtistRequest request
    ) {
        ArtistResponse updated = artistService.updateArtist(id, request);
        return ResponseEntity.ok(updated);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ArtistResponse>>> getAllArtists() {
        var artists = artistService.getAllArtists();
        return ResponseEntity.ok(new ApiResponse<>("Lista de artistas", artists));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ArtistResponse>> getArtistById(@PathVariable Long id) {
        var artist = artistService.getArtistById(id);
        return ResponseEntity.ok(new ApiResponse<>("Artista encontrado", artist));
    }
}