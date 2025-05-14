package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.model.Playlist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.SongRepository;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaylistMapper {

    private final SongRepository songRepository; // Inyección de repositorio de canciones
    private final UserRepository userRepository; // Inyección de repositorio de usuarios

    // **Convertir Playlist a PlaylistResponse**
    public PlaylistResponse toResponse(Playlist playlist) {
        PlaylistResponse playlistResponse = new PlaylistResponse(
                playlist.getPlaylistId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.isPublic(),
                playlist.getCreatedAt(),
                playlist.getSongs().stream()
                        .map(this::toSongResponse) // Mapear canciones a SongResponse
                        .collect(Collectors.toSet()),
                playlist.getUsers().stream()
                        .map(User::getId) // Extraer el ID de los usuarios
                        .collect(Collectors.toSet())
        );
        return playlistResponse;
    }

    // **Convertir PlaylistRequest a Playlist**
    public Playlist toEntity(PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.name());
        playlist.setDescription(request.description());
        playlist.setPublic(request.isPublic());
        playlist.setSongs(idsToSongs(request.songIds())); // Maneja bien Set<Long>
        playlist.setUsers(idsToUsers(request.userIds())); // Maneja bien Set<Long>
        return playlist;
    }

    // **Mapear Song a SongResponse**
    private SongResponse toSongResponse(Song song) {
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.isPublicSong(),
                song.getReleaseDate(),
                null, // Mensaje adicional, puede dejarse en blanco o configurarse
                song.getArtist().getId(),
                song.getArtist().getName()
        );
    }

    // **Convertir un Set de IDs de canciones a entidades Song**
    public Set<Song> idsToSongs(Set<Long> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return Set.of();
        }
        return songIds.stream()
                .map(songId -> songRepository.findById(songId)
                        .orElseThrow(() -> new IllegalArgumentException("Canción no encontrada con ID: " + songId)))
                .collect(Collectors.toSet());
    }

    // **Convertir un Set de IDs de usuarios a entidades User**
    public Set<User> idsToUsers(Set<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Set.of();
        }
        return userIds.stream()
                .map(userId -> userRepository.findById(userId)
                        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado con ID: " + userId)))
                .collect(Collectors.toSet());
    }
}