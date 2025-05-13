package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.dto.request.UpdateArtistRequest;
import com.dylabs.zuko.mapper.ArtistMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    private final ArtistMapper artistMapper;
    private final UserRepository userRepository;

    public ArtistResponse createArtist(CreateArtistRequest request, String username) {
        // 1. Buscar al usuario por su username
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado para el nombre: " + username));

        // 2. Verificar si el nombre del artista ya está en uso
        if (artistRepository.findByName(request.name()).isPresent()) {
            throw new ArtistAlreadyExistsException("El nombre del artista ya está en uso.");
        }

        // 3. Verificar si el usuario ya tiene un artista registrado
        if (artistRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new ArtistAlreadyExistsException("El usuario ya tiene un artista registrado.");
        }

        // 4. Mapear y guardar el nuevo artista
        Artist artist = artistMapper.toEntity(request, currentUser);
        Artist savedArtist = artistRepository.save(artist);

        return artistMapper.toResponse(savedArtist);
    }
    public ArtistResponse updateArtist(Long id, UpdateArtistRequest request) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));

        // Validar si el nombre ya está en uso por otro artista
        if (request.name() != null && !request.name().equals(artist.getName())) {
            artistRepository.findByName(request.name()).ifPresent(existingArtist -> {
                throw new ArtistAlreadyExistsException("El nombre del artista ya está en uso.");
            });
            artist.setName(request.name());
        }

        if (request.name() != null) {
            artist.setName(request.name());
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

}
