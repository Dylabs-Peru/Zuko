package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.artistExeptions.ArtistValidationException;
import com.dylabs.zuko.dto.request.UpdateArtistRequest;
import com.dylabs.zuko.mapper.ArtistMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;
    private final UserRepository userRepository;

    //Crear artista
    public ArtistResponse createArtist(CreateArtistRequest request, String username) {
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado para el nombre: " + username));

        if (artistRepository.findByName(request.name()).isPresent()) {
            throw new ArtistAlreadyExistsException("El nombre del artista ya está en uso.");
        }

        if (artistRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new ArtistAlreadyExistsException("El usuario ya tiene un artista registrado.");
        }

        Artist artist = artistMapper.toEntity(request, currentUser);
        artist.setIsActive(true);
        Artist savedArtist = artistRepository.save(artist);

        return artistMapper.toResponse(savedArtist);
    }

    //Actualizar artista
    public ArtistResponse updateArtist(Long id, UpdateArtistRequest request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));

        if (request.name() != null && !request.name().equals(artist.getName())) {
            artistRepository.findByName(request.name()).ifPresent(existingArtist -> {
                throw new ArtistAlreadyExistsException("El nombre del artista ya está en uso.");
            });
            artist.setName(request.name());
        }

        if (request.name() != null) {
            artist.setName(request.name());
        }

        if (request.country() != null && request.country().trim().isEmpty()) {
            throw new ArtistValidationException("El país no puede estar vacío.");
        }

        if (request.country() != null) {
            artist.setCountry(request.country());
        }

        if (request.biography() != null) {
            artist.setBiography(request.biography());
        }

        Artist updatedArtist = artistRepository.save(artist);
        return artistMapper.toResponse(updatedArtist);
    }

    // Para validar al dueño
    public ArtistResponse updateArtist(Long id, UpdateArtistRequest request, String username) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));

        if (!artist.getUser().getId().toString().equals(username)) {
            User user = userRepository.findById(Long.valueOf(username))
                    .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado para el id: " + username));
            if (!"ADMIN".equalsIgnoreCase(user.getUserRoleName())) {
                throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para editar este artista");
            }
        }
        return updateArtist(id, request);
    }

    // Obtener todos los artistas
    public List<ArtistResponse> getAllArtists() {
        List<Artist> artists = artistRepository.findAll();
        return artistMapper.toResponseList(artists);
    }

    public ArtistResponse getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));
        return artistMapper.toResponse(artist);
    }

    public List<ArtistResponse> searchArtistsByName(String name) {
        List<Artist> artists = artistRepository.findByNameContainingIgnoreCase(name);
        return artistMapper.toResponseList(artists);
    }

    // Para alternar el estado del artista
    public void toggleArtistActiveStatus(Long id, String username) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));

        if (!artist.getUser().getId().toString().equals(username)) {
            User user = userRepository.findById(Long.valueOf(username))
                    .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado para el id: " + username));
            if (!"ADMIN".equalsIgnoreCase(user.getUserRoleName())) {
                throw new org.springframework.security.access.AccessDeniedException("No tienes permiso para cambiar el estado de este artista");
            }
        }
        artist.setIsActive(!artist.getIsActive());
        artistRepository.save(artist);
    }

}
