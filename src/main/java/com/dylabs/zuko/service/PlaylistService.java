package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.request.UpdatePlaylistRequest;
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

    public PlaylistResponse createPlaylist(String userId, PlaylistRequest playlistRequest) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));

        if (playlistRepository.existsByNameIgnoreCaseAndUser_id(playlistRequest.name(), user.getId())) {
            throw new PlaylistAlreadyExistsException("Playlist con el nombre '" + playlistRequest.name() + "' ya existe para el usuario.");
        }

        Playlist playlist = playlistMapper.toEntity(playlistRequest);
        playlist.setUser(user);
        Playlist savedPlaylist = playlistRepository.save(playlist);
        return playlistMapper.toResponse(savedPlaylist);
    }

    public PlaylistResponse getPlaylistById(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(()-> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isPublic = playlist.isPublic();
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");

        if (!isOwner && !isPublic && !isAdmin) {
            throw new PlaylistNotPublicException("No tienes acceso a esta playlist privada");
        }
        return playlistMapper.toResponse(playlist);
    }

    public void deletePlaylist(String userId, Long playlistId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));

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

    public List<SongResponse> listSongsInPlaylist(String userId, Long playlistId) {
        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado: " + userId));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isPublic = playlist.isPublic();
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");
        if (!isOwner && !isPublic && !isAdmin) {
            throw new PlaylistNotPublicException("No tienes acceso a esta playlist privada " + user.getUserRoleName());
        }
            return playlist.getSongs().stream()
                    .map(playlistMapper::toSongResponse)
                    .collect(Collectors.toList());
    }

    public void addSongToPlaylist(String userId, Long playlistId, Long songId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado con id: " + userId));

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

    public void removeSongFromPlaylist(String userId, Long playlistId, Long songId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado con id: " + userId));

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

    public PlaylistResponse getPlaylistByName(String userId, String playlistName) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));
        Playlist playlist = playlistRepository.findByNameIgnoreCaseAndUser_id(playlistName, user.getId())
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con nombre: " + playlistName));
        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");
        boolean isPublic = playlist.isPublic();
        if (!isOwner && !isAdmin && !isPublic) {
            throw new PlaylistNotPublicException("No tienes acceso a esta playlist privada");
        }
        return playlistMapper.toResponse(playlist);
    }

    public List<PlaylistResponse> getAllPlaylistsByUser(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));
        return playlistRepository.findAllByUser_Id(user.getId())
                .stream().map(playlistMapper::toResponse)
                .collect(Collectors.toList());
    }

    public PlaylistResponse getPublicPlaylistByName(String playlistName) {
        Playlist playlist = playlistRepository.findByNameIgnoreCase(playlistName)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con nombre: " + playlistName));
        if (!playlist.isPublic()) {
            throw new PlaylistNotPublicException("La playlist es privada");
        }
        return playlistMapper.toResponse(playlist);
    }

    public PlaylistResponse editPlaylistById(Long playlistId, String userId, UpdatePlaylistRequest updatePlaylistRequest) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado con id: " + userId));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con ID: " + playlistId));

        boolean isOwner = playlist.getUser().getId().equals(user.getId());
        boolean isAdmin = user.getUserRoleName().equalsIgnoreCase("ADMIN");
        if (!isOwner && !isAdmin) {
            throw new PlaylistAccessDeniedException("No tienes permisos para modificar esta playlist.");
        }

        if (updatePlaylistRequest.name() !=null)
        {
            playlist.setName(updatePlaylistRequest.name());
        }
        if (updatePlaylistRequest.description() !=null){
            playlist.setDescription(updatePlaylistRequest.description());
        }

        playlist.setPublic(updatePlaylistRequest.isPublic());

        if (updatePlaylistRequest.url_image() !=null){
            playlist.setUrl_image(updatePlaylistRequest.url_image());
        }

        return playlistMapper.toResponse(playlist);


    }





}