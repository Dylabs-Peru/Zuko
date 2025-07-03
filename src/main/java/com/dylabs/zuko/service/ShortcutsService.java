package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.AddPlaylistToShortcutsRequest;
import com.dylabs.zuko.dto.response.ShortcutsResponse;
import com.dylabs.zuko.exception.playlistExceptions.PlaylistNotFoundException;
import com.dylabs.zuko.exception.shortcutsExceptions.PlaylistAlreadyInShortcutsException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.mapper.ShortcutsMapper;
import com.dylabs.zuko.model.Playlist;
import com.dylabs.zuko.model.Shortcuts;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.PlaylistRepository;
import com.dylabs.zuko.repository.ShortcutsRepository;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.repository.AlbumRepository;

import java.util.Set;

@Service
@RequiredArgsConstructor
public class ShortcutsService {
    private final ShortcutsRepository shortcutsRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final ShortcutsMapper shortcutsMapper;
    private final AlbumRepository albumRepository;

    public void addPlaylistToShortcuts(String userId, AddPlaylistToShortcutsRequest request) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));

        Shortcuts shortcuts = shortcutsRepository.findByUser_Id(Long.parseLong(userId))
                .orElseThrow(()->new UserNotFoundExeption("Shortcut no encontrado para usuario con id: " + userId));

        Playlist playlist = playlistRepository.findById(request.playlistId())
                .orElseThrow(() -> new PlaylistNotFoundException("Playlist no encontrada con id: " + request.playlistId()));

        if (shortcutsRepository.existsByUser_IdAndPlaylists_PlaylistId(Long.parseLong(userId), request.playlistId())) {
            throw new PlaylistAlreadyInShortcutsException("La playlist ya está en tus accesos directos");
        }
        shortcuts.getPlaylists().add(playlist);
        shortcutsRepository.save(shortcuts);
    }

    public void removePlaylistFromShortcuts(String userId, Long playlistId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));

        Shortcuts shortcuts = shortcutsRepository.findByUser_Id(Long.parseLong(userId))
                .orElseThrow(()->new UserNotFoundExeption("Shortcut no encontrado para usuario con id: " + userId));

        Playlist playlist = playlistRepository.findById(playlistId)
                .orElseThrow(() -> new PlaylistNotFoundException("No existe playlist en tus accesos directos con id: " + playlistId));

        shortcuts.getPlaylists().remove(playlist);
        shortcutsRepository.save(shortcuts);
    }

    public ShortcutsResponse getShortcutsByUser(String userId) {
        User user = userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + userId));
        Shortcuts shortcuts = shortcutsRepository.findByUser_Id(Long.parseLong(userId))
                .orElseThrow(()->new UserNotFoundExeption("Shortcut no encontrado para usuario con id:" + userId));
        return shortcutsMapper.toShortcutsResponse(shortcuts);
    }

    public void addAlbumToUserShortcuts(Long userId, Long albumId) {
        Shortcuts shortcuts = shortcutsRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron accesos directos para el usuario con ID: " + userId));

        if (shortcutsRepository.existsByUser_IdAndAlbums_Id(userId, albumId)) {
            throw new IllegalStateException("El álbum ya está en los accesos directos");
        }

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un álbum con ID: " + albumId));

        shortcuts.getAlbums().add(album); // Asocia el álbum a los accesos directos
        shortcutsRepository.save(shortcuts);
    }

    // Metodo para eliminar un álbum de los accesos directos de un usuario
    public void removeAlbumFromUserShortcuts(Long userId, Long albumId) {
        Shortcuts shortcuts = shortcutsRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron accesos directos para el usuario con ID: " + userId));

        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontró un álbum con ID: " + albumId));

        if (!shortcuts.getAlbums().contains(album)) {
            throw new IllegalStateException("El álbum no está en los accesos directos");
        }
        shortcuts.getAlbums().remove(album);
        shortcutsRepository.save(shortcuts);
    }

    // Metodo para listar los álbumes de los accesos directos de un usuario
    public Set<Album> getUserShortcutsAlbums(Long userId) {
        Shortcuts shortcuts = shortcutsRepository.findByUser_Id(userId)
                .orElseThrow(() -> new IllegalArgumentException("No se encontraron accesos directos para el usuario con ID: " + userId));

        return shortcuts.getAlbums();
    }

}
