package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<SongResponse> createSong(@RequestBody @Valid SongRequest request, Authentication authentication) {
        // Obtienes el username/id del token decodificado por Spring Security
        String username = authentication.getName();

        SongResponse response = songService.createSong(request, username);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<SongResponse> updateSong(@PathVariable Long id, @RequestBody @Valid SongRequest request, Authentication authentication) {
        String username = authentication.getName();
        SongResponse response = songService.updateSong(id, request, username);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<SongResponse>> getOwnSongs(Authentication authentication) {
        String username = authentication.getName();
        List<SongResponse> songs = songService.getSongsByUser(username);
        return ResponseEntity.ok(songs);
    }

    @GetMapping("/search")
    public ResponseEntity<List<SongResponse>> searchSongs(@RequestParam String title) {
        List<SongResponse> songs = songService.searchPublicSongsByTitle(title);
        return ResponseEntity.ok(songs);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public SongResponse deleteSong(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        return songService.deleteSong(id, username);
    }
}
