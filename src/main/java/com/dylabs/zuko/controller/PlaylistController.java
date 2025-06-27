package com.dylabs.zuko.controller;
import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.AddSongtoPlaylistRequest;
import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.request.UpdatePlaylistRequest;
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
        String userId = authentication.getName();
        PlaylistResponse response = playlistService.createPlaylist(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Playlist creada correctamente", response));
    }

    @GetMapping("/{playlistId}")
    public ResponseEntity<Object> getPlaylistById(@PathVariable Long playlistId, Authentication authentication) {
        String userId = authentication.getName();
        PlaylistResponse response = playlistService.getPlaylistById(userId, playlistId);
        return ResponseEntity.ok(
                new ApiResponse<>("Playlist obtenida correctamente", response));
    }

    @DeleteMapping("/{playlistId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> deletePlaylist(@PathVariable Long playlistId, Authentication authentication) {
        String userId = authentication.getName();
        playlistService.deletePlaylist(userId, playlistId);
        return ResponseEntity
                .ok(new ApiResponse<>("Playlist eliminada correctamente", null));
    }

    @GetMapping("/{playlistId}/songs")
    public ResponseEntity<Object> listSongsInPlaylist(@PathVariable Long playlistId, Authentication authentication) {
        String userId = authentication.getName();
        List<SongResponse> songs = playlistService.listSongsInPlaylist(userId, playlistId);
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
        String userId = authentication.getName();
        playlistService.addSongToPlaylist(userId, playlistId, request.songId());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new ApiResponse<>("Canción añadida correctamente", null));
    }

    @DeleteMapping("/{playlistId}/songs/{songId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> removeSongFromPlaylist(
            @PathVariable Long playlistId,
            @PathVariable Long songId,
            Authentication authentication) {
        String userId = authentication.getName();
        playlistService.removeSongFromPlaylist(userId, playlistId, songId);
        return ResponseEntity.ok(
                new ApiResponse<>("Canción eliminada correctamente", null));
    }

    @GetMapping("/public/search")
    public ResponseEntity<Object> searchPublicPlaylistsByName(@RequestParam String name) {
        List<PlaylistResponse> playlists = playlistService.searchPublicPlaylistsByName(name);
        return ResponseEntity.ok(new ApiResponse<>("Playlists encontradas", playlists));
    }

    @GetMapping("/by-name/{playlistName}")
    public ResponseEntity<Object> getPlaylistByName(
            @PathVariable String playlistName, Authentication authentication) {
        String userId = authentication.getName();
        PlaylistResponse response = playlistService.getPlaylistByName(userId, playlistName);
        return ResponseEntity.ok(new ApiResponse<>("Playlist obtenida correctamente", response));
    }

    @GetMapping("/mine")
    public ResponseEntity<Object> getMyPlaylists(Authentication authentication) {
        String userId = authentication.getName();
        List<PlaylistResponse> response = playlistService.getAllPlaylistsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("Playlists obtenidas correctamente", response));
    }

    @PatchMapping("/{playlistId}")
    public ResponseEntity<Object> updatePlaylist(
            @PathVariable Long playlistId,
            @Valid
            @RequestBody UpdatePlaylistRequest updatePlaylistRequest,
            Authentication authentication) {
        String userId = authentication.getName();
        PlaylistResponse response = playlistService.editPlaylistById(playlistId, userId, updatePlaylistRequest);
        return ResponseEntity.ok(new ApiResponse<>("Playlist editada correctamente", response));

    }

    @GetMapping("/mine/search")
    public ResponseEntity<Object> searchMyPlaylistsByName(
            @RequestParam String name,
            Authentication authentication
    ) {
        String userId = authentication.getName();
        List<PlaylistResponse> playlists = playlistService.searchMyPlaylistsByName(userId, name);
        return ResponseEntity.ok(new ApiResponse<>("Playlists encontradas", playlists));
    }







}