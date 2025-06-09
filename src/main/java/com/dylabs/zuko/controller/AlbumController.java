package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.service.AlbumService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/albums")
@RequiredArgsConstructor
public class AlbumController {

    private final AlbumService albumService;
    private final com.dylabs.zuko.repository.UserRepository userRepository;
    private final com.dylabs.zuko.repository.ArtistRepository artistRepository;



    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> createAlbum(@RequestBody @Valid AlbumRequest request, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (!isAdmin) {
            boolean hasArtistProfile = userRepository.findByUsername(username)
                    .flatMap(user -> artistRepository.findByUserId(user.getId()))
                    .isPresent();
            if (!hasArtistProfile) {
                throw new AccessDeniedException("Debes tener un perfil de artista para crear álbumes.");
            }
        }
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
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> updateAlbum(@PathVariable Long id, @RequestBody @Valid AlbumRequest request, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        AlbumResponse album = albumService.getAlbumById(id);
        Long albumArtistId = album.artistId();
        Long userArtistId = userRepository.findByUsername(username)
                .flatMap(user -> artistRepository.findByUserId(user.getId()))
                .map(artist -> artist.getId())
                .orElse(null);
        if (!isAdmin) {
            if (userArtistId == null) {
                throw new AccessDeniedException("Debes tener un perfil de artista para editar álbumes.");
            }
            if (!albumArtistId.equals(userArtistId)) {
                throw new AccessDeniedException("No tienes permiso para editar este álbum.");
            }
        }
        AlbumResponse response = albumService.updateAlbum(id, request, username);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum actualizado correctamente",
                        "data", response
                )
        );
    }



    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<Object> deleteAlbum(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        AlbumResponse album = albumService.getAlbumById(id);

        albumService.deleteAlbum(id, username);
        return ResponseEntity.ok(
                Map.of(
                        "message", "Álbum eliminado correctamente"
                )
        );
    }
}
