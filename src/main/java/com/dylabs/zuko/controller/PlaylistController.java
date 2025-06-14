package com.dylabs.zuko.controller;
import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.AddSongtoPlaylistRequest;
import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.service.PlaylistService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import java.util.List;


@RestController
@RequestMapping("playlists")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> createPlaylist(@RequestBody @Valid PlaylistRequest request, Authentication authentication) {
        String id = authentication.getName();
        PlaylistResponse response = playlistService.createPlaylist(id, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Playlist creada correctamente", response));
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<Object> getPlaylistById(@PathVariable Long playlistId, Authentication authentication) {
        String username = authentication.getName();
        PlaylistResponse response = playlistService.getPlaylistById(username, playlistId);
        return ResponseEntity.ok(
                new ApiResponse<>("Playlist obtenida correctamente", response));
    }

    @DeleteMapping("/{playlistId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> deletePlaylist(@PathVariable Long playlistId, Authentication authentication) {
        String username = authentication.getName();
        playlistService.deletePlaylist(username, playlistId);
        return ResponseEntity
                .ok(new ApiResponse<>("Playlist eliminada correctamente", null));
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<Object> listSongsInPlaylist(@PathVariable Long playlistId, Authentication authentication) {
        String username = authentication.getName();
        List<SongResponse> songs = playlistService.listSongsInPlaylist(username, playlistId);
        return ResponseEntity.ok(
                new ApiResponse<>("Lista de songs obtenida correctamente", songs)
        );
    }
    @PostMapping("/{playlistId}/songs")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> addSongToPlaylist(
            @PathVariable Long playlistId,
            @RequestBody @Valid AddSongtoPlaylistRequest request,
            Authentication authentication) {
        String username = authentication.getName();
        playlistService.addSongToPlaylist(username, playlistId, request.songId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Canción añadida correctamente", null));
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            Authentication authentication) {
        String username = authentication.getName();
        playlistService.removeSongFromPlaylist(username, playlistId, songId);
        return ResponseEntity.ok(
                new ApiResponse<>("Canción eliminada correctamente", null));
    }



}