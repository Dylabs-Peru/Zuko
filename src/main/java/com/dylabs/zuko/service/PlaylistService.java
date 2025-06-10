package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistNotFoundException;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistAlreadyExistsException;
import com.dylabs.zuko.exception.playlistExceptions.SongNotInPlaylistException;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
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

    public PlaylistResponse createPlaylist(Long id, PlaylistRequest playlistRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado con ID: " + id));

        // Verificar si ya existe una playlist con el mismo nombre para el usuario
        if (playlistRepository.existsByNameIgnoreCaseAndUsers_id(playlistRequest.name(), id)) {
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
    public PlaylistResponse getPlaylistById(Long id, Long playlistId) {
        // Verificar si existe la Playlist asociada al usuario
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_id(playlistId, id)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + id));

        // Retornar la respuesta mapeada
        return playlistMapper.toResponse(playlist);
    }

    // Eliminar una Playlist por ID
    public void deletePlaylist(Long id, Long playlistId) {
        // Recuperar la Playlist asociada al usuario
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_id(playlistId, id)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + id));

        // Eliminar la Playlist
        playlistRepository.delete(playlist);
    }

    // Listar canciones en una Playlist
    public List<SongResponse> listSongsInPlaylist(Long id, Long playlistId) {
        // Recuperar la Playlist
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_id(playlistId, id)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + id));

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
    public void addSongToPlaylist(Long id, Long playlistId, Long songId) {
        // Recuperar la Playlist y la Canción
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_id(playlistId, id)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + id));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada con ID: " + songId));

        // Agregar la canción y guardar
        playlist.getSongs().add(song);
        playlistRepository.save(playlist);
    }

    // Eliminar una canción de una Playlist
    public void removeSongFromPlaylist(Long id, Long playlistId, Long songId) {
        // Recuperar la Playlist y la Canción
        Playlist playlist = playlistRepository.findByPlaylistIdAndUsers_id(playlistId, id)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId + " para el usuario con ID: " + id));

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada con ID: " + songId));

        // Eliminar la canción y guardar
        if (!playlist.getSongs().remove(song)) {
            throw new SongNotInPlaylistException("La canción con ID: " + songId + " no pertenece a la Playlist.");
        }

        playlistRepository.save(playlist);
    }
}