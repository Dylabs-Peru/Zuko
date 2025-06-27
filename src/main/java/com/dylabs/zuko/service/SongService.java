package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.songExceptions.SongAlreadyExistException;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.SongRepository;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor

public class SongService {

    private final SongRepository repository;
    private final SongMapper songMapper;
    private final ArtistRepository artistRepository;
    private final UserRepository userRepository;

    // Crear canción
    public SongResponse createSong(SongRequest request, String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Artist artist;
        if (user.getUserRoleName().equals("ADMIN")) {
            artist = artistRepository.findById(request.artistId())
                    .orElseThrow(() -> new ArtistNotFoundException("El artista con ID " + request.artistId() + " no existe"));
        } else {
            artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));
        }

        boolean exists = repository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId());
        if (exists) {
            throw new SongAlreadyExistException("La canción ya está registrada para este artista.");
        }

        Song song = songMapper.toSongEntity(request, artist);
        song.setReleaseDate(LocalDate.now());

        Song saved = repository.save(song);
        return songMapper.toResponse(saved);
    }

    public List<SongResponse> getSongsByUser(String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));

        List<Song> songs = repository.findAllByArtistId(artist.getId());

        if (songs.isEmpty()) {
            throw new SongNotFoundException("Aún no has registrado canciones.");
        }

        return songs.stream()
                .map(songMapper::toResponse)
                .toList();
    }

    public SongResponse getSongById(Long id) {
        Song song = repository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada con ID: " + id));
        return songMapper.toResponse(song);
    }

    public List<SongResponse> searchPublicSongsByTitle(String title) {
        var songs = repository.findByTitleContainingIgnoreCaseAndIsPublicSongTrue(title);

        if (songs.isEmpty()) {
            throw new SongNotFoundException("No existe la canción buscada.");
        }

        return songs.stream()
                .map(songMapper::toResponse)
                .toList();
    }

    // Editar canción
    public SongResponse updateSong(Long id, SongRequest request, String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Song song = repository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada"));

        if (!user.getUserRoleName().equals("ADMIN")) {
            Artist artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));
            if (!song.getArtist().getId().equals(artist.getId())) {
                throw new AccessDeniedException("No puedes modificar esta canción.");
            }
        }

        if (user.getUserRoleName().equals("ADMIN")) {
            Artist newArtist = artistRepository.findById(request.artistId())
                    .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado"));
            song.setArtist(newArtist);
        }

        song.setTitle(request.title());
        song.setPublicSong(request.isPublicSong());
        song.setYoutubeUrl(request.youtubeUrl());
        song.setImageUrl(request.imageUrl());

        Song updated = repository.save(song);

        return new SongResponse(
                updated.getId(),
                updated.getTitle(),
                updated.isPublicSong(),
                updated.getReleaseDate(),
                "La canción ha sido actualizada correctamente.",
                updated.getArtist().getId(),
                updated.getArtist().getName(),
                updated.getYoutubeUrl(),
                updated.getImageUrl()

        );
    }

    // Eliminar canción
    public SongResponse deleteSong(Long id, String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));

        Song song = repository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("La canción no se encontró."));

        if (!user.getUserRoleName().equals("ADMIN")) {
            Artist artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));
            if (!song.getArtist().getId().equals(artist.getId())) {
                throw new AccessDeniedException("No puedes eliminar esta canción.");
            }
        }

        repository.delete(song);

        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.isPublicSong(),
                song.getReleaseDate(),
                "La canción ha sido eliminada correctamente.",
                song.getArtist().getId(),
                song.getArtist().getName(),
                song.getYoutubeUrl(),
                song.getImageUrl()

        );
    }

    public List<SongResponse> getSongsByArtistId(Long artistId) {
        return repository.findAllByArtistId(artistId).stream()
                .map(songMapper::toResponse)
                .toList();
    }
}