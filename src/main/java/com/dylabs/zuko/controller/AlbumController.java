package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> createAlbum(@RequestBody @Valid AlbumRequest request, Authentication authentication) {

        String userIdFromToken = authentication.getName();
        AlbumResponse response = albumService.createAlbum(request, userIdFromToken);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Álbum creado correctamente",
                        "data", response
                )
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> getAlbumById(@PathVariable Long id, Authentication authentication) {

        String userIdFromToken = authentication.getName();
        AlbumResponse response = albumService.getAlbumById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum obtenido correctamente",
                        "data", response
                )
        );
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> getAlbumsByTitle(@RequestParam String title) {
        List<AlbumResponse> response = albumService.getAlbumsByTitle(title);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbumes obtenidos correctamente",
                        "data", response
                )
        );
    }

    @GetMapping("/search/artist")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> getAlbumsByTitleAndUser(@RequestParam String title, Authentication authentication) {
        String userIdFromToken = authentication.getName();
        List<AlbumResponse> response = albumService.getAlbumsByTitleAndUser(title, userIdFromToken);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbumes obtenidos correctamente para el artista",
                        "data", response
                )
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<AlbumResponse>>> getAllAlbums() {
        List<AlbumResponse> response = albumService.getAllAlbums();
        return ResponseEntity.ok(new ApiResponse<>("Álbumes obtenidos correctamente", response));
    }


    @GetMapping("/by-artist/{artistId}")
    public ResponseEntity<List<AlbumResponse>> getAlbumsByArtist(@PathVariable Long artistId) {
        return ResponseEntity.ok(albumService.getAlbumsByArtistId(artistId));
    }


    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> updateAlbum(@PathVariable Long id, @RequestBody @Valid AlbumRequest request, Authentication authentication) {

        String userIdFromToken = authentication.getName();
        AlbumResponse response = albumService.updateAlbum(id, request, userIdFromToken);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum actualizado correctamente",
                        "data", response
                )
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> deleteAlbum(@PathVariable Long id, Authentication authentication) {

        String userIdFromToken = authentication.getName();
        albumService.deleteAlbum(id, userIdFromToken);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum eliminado correctamente"
                )
        );
    }

    @GetMapping("/from-song/{songId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> getAlbumBySongId(@PathVariable Long songId) {
        AlbumResponse response = albumService.getAlbumBySongId(songId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum encontrado por ID de canción",
                        "data", response
                )
        );
    }

}