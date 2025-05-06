package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("songs")
@RequiredArgsConstructor
public class SongController {

    private final SongService songService;

    @PostMapping
    public ResponseEntity<SongResponse> createSong(@RequestBody @Valid SongRequest request) {
        SongResponse response = songService.createSong(request);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<SongResponse> updateSong(@PathVariable Long id, @RequestBody @Valid SongRequest request) {
        SongResponse response = songService.updateSong(id, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public SongResponse deleteSong(@PathVariable Long id) {
        return songService.deleteSong(id);
    }
}
