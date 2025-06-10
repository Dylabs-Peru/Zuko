package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistAlreadyExistsException;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistNotFoundException;
import com.dylabs.zuko.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/users/{userId}/playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    public ResponseEntity<Object> createPlaylist(
            @PathVariable Long userId,
            @RequestBody @Valid PlaylistRequest request) {
        PlaylistResponse response = playlistService.createPlaylist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Playlist creada correctamente", response));
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<Object> getPlaylistById(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {
        PlaylistResponse response = playlistService.getPlaylistById(userId, playlistId);
        return ResponseEntity.ok(
                new ApiResponse<>("Playlist obtenida correctamente", response));
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Object> deletePlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {
        playlistService.deletePlaylist(userId, playlistId);
        return ResponseEntity
                .ok(new ApiResponse<>("Playlist eliminada correctamente", null));
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<Object> listSongsInPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId) {
        List<SongResponse> songs = playlistService.listSongsInPlaylist(userId, playlistId);
        return ResponseEntity.ok(
                new ApiResponse<>("Lista de songs obtenida correctamente", songs)
        );
    }
    @PostMapping("/{playlistId}/songs")
    public ResponseEntity<Object> addSongToPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @RequestParam Long songId) {
        playlistService.addSongToPlaylist(userId, playlistId, songId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Cancion añadida correctamente", null)
        );
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    public ResponseEntity<Object> removeSongFromPlaylist(
            @PathVariable Long userId,
            @PathVariable Long playlistId,
            @PathVariable Long songId) {
        playlistService.removeSongFromPlaylist(userId, playlistId, songId);
        return ResponseEntity.ok(
                new ApiResponse<>("Cancion eliminada correctamente", null)
        );
    }


}