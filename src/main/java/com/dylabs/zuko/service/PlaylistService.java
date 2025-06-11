package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.playlistExceptions.*;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
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

    public PlaylistResponse createPlaylist(String id, PlaylistRequest playlistRequest) {
        User user = userRepository.findById(Long.parseLong(id))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + id));

        if (playlistRepository.existsByNameIgnoreCaseAndUser_id(playlistRequest.name(), user.getId())) {
            throw new PlaylistAlreadyExistsException("Playlist con el nombre '" + playlistRequest.name() + "' ya existe para el usuario.");
        }

        Playlist playlist = playlistMapper.toEntity(playlistRequest);
        playlist.setUser(user);
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toResponse(savedPlaylist);
    }

    public PlaylistResponse getPlaylistById(String username, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        User user = userRepository.findById(Long.parseLong(username))
                .orElseThrow(()-> new UserNotFoundExeption("Usuario no encontrado con id: " + username));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isPublic = playlist.isPublic();

        if (!isOwner && !isPublic) {
            throw new PlaylistNotPublicException("No tienes acceso a esta playlist privada");
        }
        return playlistMapper.toResponse(playlist);
    }

    public void deletePlaylist(String username, Long playlistId) {
        User user = userRepository.findById(Long.parseLong(username))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con email: " + username));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException(
                        "Playlist no encontrada con ID: " + playlistId));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new PlaylistAccessDeniedException("No tienes permisos para eliminar esta playlist.");
        }

        playlistRepository.delete(playlist);
    }

    public List<SongResponse> listSongsInPlaylist(String username, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        User user = userRepository.findById(Long.parseLong(username))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado: " + username));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isPublic = playlist.isPublic();
        if (!isOwner && !isPublic) {
            throw new PlaylistNotPublicException("No tienes acceso a esta playlist privada");
        }
            return playlist.getSongs().stream()
                    .map(playlistMapper::toSongResponse)
                    .collect(Collectors.toList());
    }

    public void addSongToPlaylist(String username, Long playlistId, Long songId) {
        User user = userRepository.findById(Long.parseLong(username))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado con id: " + username));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new PlaylistAccessDeniedException("No tienes permisos para modificar esta playlist.");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada con ID: " + songId));

        playlist.getSongs().add(song);
        playlistRepository.save(playlist);
    }

    public void removeSongFromPlaylist(String username, Long playlistId, Long songId) {
        User user = userRepository.findById(Long.parseLong(username))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado con id: " + username));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new PlaylistAccessDeniedException("No tienes permisos para modificar esta playlist.");
        }

        Song song = songRepository.findById(songId)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada con ID: " + songId));

        if (!playlist.getSongs().remove(song)) {
            throw new SongNotInPlaylistException("La canción con ID: " + songId + " no pertenece a la Playlist.");
        }

        playlistRepository.save(playlist);
    }

}