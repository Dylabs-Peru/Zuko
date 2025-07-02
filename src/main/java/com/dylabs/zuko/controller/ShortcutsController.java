package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.AddPlaylistToShortcutsRequest;
import com.dylabs.zuko.dto.response.ShortcutsResponse;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.service.ShortcutsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@RestController
@RequestMapping("shortcuts")
@RequiredArgsConstructor
public class ShortcutsController {
    private final ShortcutsService shortcutsService;

    @PostMapping
    public ResponseEntity<Object> addPlaylistToShortCuts(@RequestBody AddPlaylistToShortcutsRequest request, Authentication authentication) {
        String userId = authentication.getName();
        shortcutsService.addPlaylistToShortcuts(userId, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Playlist agregada a accesos directos", null));
    }

    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Object> removePlaylistFromShortcuts(@PathVariable long playlistId, Authentication authentication ) {
        String userId = authentication.getName();
        shortcutsService.removePlaylistFromShortcuts(userId, playlistId);
        return ResponseEntity
                .ok(new ApiResponse<>("Playlist removida de accesos directos", null));
    }

    @GetMapping
    public ResponseEntity<Object> getShortcutsByUser(Authentication authentication) {
        String userId = authentication.getName();
        ShortcutsResponse response = shortcutsService.getShortcutsByUser(userId);
        return ResponseEntity.ok(new ApiResponse<>("Shortcuts de accesos directos", response));
    }

    @PostMapping("/albums")
    public ResponseEntity<Object> addAlbumToShortcuts(@RequestParam Long albumId, Authentication authentication) {
        String userId = authentication.getName();
        shortcutsService.addAlbumToUserShortcuts(Long.parseLong(userId), albumId);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                new ApiResponse<>("Álbum agregado a accesos directos", null));
    }

    @DeleteMapping("/albums/{albumId}")
    public ResponseEntity<Object> removeAlbumFromShortcuts(@PathVariable Long albumId, Authentication authentication) {
        String userId = authentication.getName();
        shortcutsService.removeAlbumFromUserShortcuts(Long.parseLong(userId), albumId);
        return ResponseEntity.ok(new ApiResponse<>("Álbum eliminado de accesos directos", null));
    }

    @GetMapping("/albums")
    public ResponseEntity<Object> getUserShortcutsAlbums(Authentication authentication) {
        String userId = authentication.getName();
        Set<Album> albums = shortcutsService.getUserShortcutsAlbums(Long.parseLong(userId));
        return ResponseEntity.ok(new ApiResponse<>("Álbumes en accesos directos", albums));
    }


}
