package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.service.SongService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
