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

import java.util.List;

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
        artist.setIsActive(true);

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
    // Obtener todos los artistas
    public List<ArtistResponse> getAllArtists() {
        List<Artist> artists = artistRepository.findAll();
        return artistMapper.toResponseList(artists);
    }

    // Obtener un artista por ID
    public ArtistResponse getArtistById(Long id) {
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));
        return artistMapper.toResponse(artist);
    }

    // obtener artista por nombre
    public List<ArtistResponse> searchArtistsByName(String name) {
        List<Artist> artists = artistRepository.findByNameContainingIgnoreCase(name);
        return artistMapper.toResponseList(artists);
    }

    public void toggleArtistActiveStatus(Long id) {
        // 1. Buscar el artista por su ID
        Artist artist = artistRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + id));

        // 2. Obtener el usuario asociado al artista
        User user = artist.getUser();

        // 3. Alternar el estado de isActive del usuario
        user.setActive(!user.getIsActive());

        // 4. Guardar el usuario con el nuevo estado
        userRepository.save(user);
    }

}
