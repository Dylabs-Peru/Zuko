package com.dylabs.zuko.mapper;
import com.dylabs.zuko.dto.request.AddPlaylistToShortcutsRequest;
import com.dylabs.zuko.dto.response.*;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Playlist;
import com.dylabs.zuko.model.Shortcuts;
import com.dylabs.zuko.model.Song;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ShortcutsMapper {
    public ShortcutsResponse toShortcutsResponse(Shortcuts shortcuts) {
           return new ShortcutsResponse(
                    shortcuts.getId(),
                    shortcuts.getPlaylists().stream()
                            .map(this::toPlaylistSummary)
                            .collect(Collectors.toSet()),
                    shortcuts.getAlbums().stream()
                            .map(this::toAlbumResponse)
                            .collect(Collectors.toSet())
            );

    }

    public PlaylistSummaryResponse toPlaylistSummary(Playlist playlist) {
        return new PlaylistSummaryResponse(
                playlist.getPlaylistId(),
                playlist.getName(),
                playlist.getUrl_image(),
                playlist.getUser().getUsername()
        );
    }

    // cambiar por uno más óptimo
    public AlbumResponse toAlbumResponse(Album album) {
        List<AlbumSongSummaryResponse> songs = album.getSongs().stream()
                .map(song -> new AlbumSongSummaryResponse(
                        song.getTitle(),
                        song.getReleaseDate()
                ))
                .collect(Collectors.toList());
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                album.getArtist().getId(),
                album.getArtist().getName(),
                album.getGenre().getName(),
                songs
        );
    }


}
