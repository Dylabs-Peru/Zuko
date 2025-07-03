package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.response.ReleaseItemResponse;
import com.dylabs.zuko.service.AlbumService;
import com.dylabs.zuko.service.SongService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/releases")
@RequiredArgsConstructor
public class ReleaseController {

    private final SongService songService;
    private final AlbumService albumService;

    @GetMapping("/top-today")
    public ResponseEntity<List<ReleaseItemResponse>> getTopReleases() {
        // Obtén las canciones y los álbumes
        List<ReleaseItemResponse> songs = songService.getTop3PublicSongsAsReleases();
        List<ReleaseItemResponse> albums = albumService.getTop3PublicAlbums();

        // Combina y ordena por la fecha de lanzamiento (descendentemente)
        List<ReleaseItemResponse> combined = new ArrayList<>();
        combined.addAll(songs);
        combined.addAll(albums);

        combined.sort(Comparator.comparing(ReleaseItemResponse::releaseDate).reversed());

        return ResponseEntity.ok(combined);
    }
}

