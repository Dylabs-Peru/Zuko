package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
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
        // 1. Buscar al usuario por su username (ya no viene en el request)
        User currentUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado para el nombre: " + username));

        // 2. Verificar si ya es artista
        if (artistRepository.findByUserId(currentUser.getId()).isPresent()) {
            throw new ArtistAlreadyExistsException("El usuario ya tiene un artista registrado.");
        }

        // 3. Mapear y guardar
        Artist artist = artistMapper.toEntity(request, currentUser);
        Artist savedArtist = artistRepository.save(artist);

        return artistMapper.toResponse(savedArtist);
    }

}
