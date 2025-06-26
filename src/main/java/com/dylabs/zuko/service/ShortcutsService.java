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

@Service
@RequiredArgsConstructor
public class ShortcutsService {
    private final ShortcutsRepository shortcutsRepository;
    private final UserRepository userRepository;
    private final PlaylistRepository playlistRepository;
    private final ShortcutsMapper shortcutsMapper;

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
}
