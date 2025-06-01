package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;

    @PostMapping
    public ResponseEntity<Object> createAlbum(@RequestBody @Valid AlbumRequest request) {
        AlbumResponse response = albumService.createAlbum(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                Map.of(
                        "message", "Álbum creado correctamente",
                        "data", response
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<Object> getAlbumById(@PathVariable Long id) {
        AlbumResponse response = albumService.getAlbumById(id);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum obtenido correctamente",
                        "data", response
                )
        );
    }

    @PutMapping("/{id}")
    public ResponseEntity<Object> updateAlbum(@PathVariable Long id, @RequestBody @Valid AlbumRequest request) {
        AlbumResponse response = albumService.updateAlbum(id, request);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum actualizado correctamente",
                        "data", response
                )
        );
    }

    // Endpoint para eliminar un álbum
    @DeleteMapping("/{id}")
    public ResponseEntity<Object> deleteAlbum(@PathVariable Long id, @RequestParam Long artistId) {
        albumService.deleteAlbum(id, artistId);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum eliminado correctamente"
                )
        );
    }
}
