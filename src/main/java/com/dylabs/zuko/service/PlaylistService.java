package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistNotFoundException;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistAlreadyExistsException;
import com.dylabs.zuko.mapper.PlaylistMapper;
import com.dylabs.zuko.model.Playlist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.PlaylistRepository;
import com.dylabs.zuko.repository.SongRepository;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final SongRepository songRepository;
    private final UserRepository userRepository;
    private final PlaylistMapper playlistMapper;

    // Crear una nueva Playlist
    public PlaylistResponse createPlaylist(Long userId, PlaylistRequest playlistRequest) {
        // Verificar si el usuario existe
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId));

        // Verificar si ya existe una playlist con el mismo nombre para el usuario
        if (playlistRepository.existsByNameIgnoreCaseAndUsers_UserId(playlistRequest.name(), userId)) {
            throw new PlaylistAlreadyExistsException("Playlist con el nombre '" + playlistRequest.name() + "' ya existe para el usuario.");
        }

        // Convertir el DTO a entidad y asociar el usuario actual
        Playlist playlist = playlistMapper.toEntity(playlistRequest);
        playlist.getUsers().add(user); // Asignar usuario propietario

        // Guardar playlist y devolver la respuesta mapeada
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toResponse(savedPlaylist);
    }

    // Obtener una Playlist por ID
    public PlaylistResponse getPlaylistById(Long userId, Long playlistId) {
        // Verificar si existe la Playlist asociada al usuario
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_UserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + userId));

        // Retornar la respuesta mapeada
        return playlistMapper.toResponse(playlist);
    }

    // Eliminar una Playlist por ID
    public void deletePlaylist(Long userId, Long playlistId) {
        // Recuperar la Playlist asociada al usuario
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_UserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + userId));

        // Eliminar la Playlist
        playlistRepository.delete(playlist);
    }

    // Listar canciones en una Playlist
    public List<SongResponse> listSongsInPlaylist(Long userId, Long playlistId) {
        // Recuperar la Playlist
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_UserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + userId));

        // Convertir las canciones a DTO de respuesta
        return playlist.getSongs().stream()
                .map(song -> new SongResponse(
                        song.getId(),
                        song.getTitle(),
                        song.isPublicSong(),
                        song.getReleaseDate(),
                        null,
                        song.getArtist().getId(),
                        song.getArtist().getName()
                ))
                .collect(Collectors.toList());
    }

    // Agregar una canción a una Playlist
    public void addSongToPlaylist(Long userId, Long playlistId, Long songId) {
        // Recuperar la Playlist y la Canción
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_UserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + userId));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada con ID: " + songId));

        // Agregar la canción y guardar
        playlist.getSongs().add(song);
        playlistRepository.save(playlist);
    }

    // Eliminar una canción de una Playlist
    public void removeSongFromPlaylist(Long userId, Long playlistId, Long songId) {
        // Recuperar la Playlist y la Canción
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_UserId(playlistId, userId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + userId));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada con ID: " + songId));

        // Eliminar la canción y guardar
        if (!playlist.getSongs().remove(song)) {
            throw new IllegalArgumentException("La canción con ID: " + songId + " no pertenece a la Playlist.");
        }

        playlistRepository.save(playlist);
    }
}