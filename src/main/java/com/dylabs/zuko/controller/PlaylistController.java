package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistAlreadyExistsException;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistNotFoundException;
import com.dylabs.zuko.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/{userId}/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    // **Crear una nueva playlist**
    @PostMapping
    public ResponseEntity<Object> createPlaylist(
            @PathVariable Long userId,
            @RequestBody @Valid PlaylistRequest request) {
        PlaylistResponse response = playlistService.createPlaylist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Playlist creada correctamente",
                        "data", response
                )
        );
    }

    // **Obtener una playlist específica**
    @GetMapping("/{playlistId}")
    public ResponseEntity<Object> getPlaylistById(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {
        PlaylistResponse response = playlistService.getPlaylistById(userId, playlistId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Playlist obtenida correctamente",
                        "data", response
                )
        );
    }

    // **Eliminar una playlist**
    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Object> deletePlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {
        playlistService.deletePlaylist(userId, playlistId);
        return ResponseEntity.noContent().build();
    }

    // **Obtener las canciones de una playlist**
    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<Object> listSongsInPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {
        return ResponseEntity.ok(
                Map.of(
                        "message", "Lista de canciones obtenida correctamente",
                        "data", playlistService.listSongsInPlaylist(userId, playlistId)
                )
        );
    }

    // **Agregar una canción a la playlist**
    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<Object> addSongToPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @RequestParam Long songId) {
        playlistService.addSongToPlaylist(userId, playlistId, songId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Canción añadida correctamente a la playlist"
                )
        );
    }

    // **Eliminar una canción de la playlist**
    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Object> removeSongFromPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(userId, playlistId, songId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Canción eliminada correctamente de la playlist"
                )
        );
    }

    // **Manejo de excepciones específicas**

    @ExceptionHandler(PlaylistAlreadyExistsException.class)
    public ResponseEntity<String> handlePlaylistAlreadyExists(PlaylistAlreadyExistsException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ex.getMessage());
    }

    @ExceptionHandler(PlaylistNotFoundException.class)
    public ResponseEntity<String> handlePlaylistNotFound(PlaylistNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleIllegalArgument(IllegalArgumentException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}