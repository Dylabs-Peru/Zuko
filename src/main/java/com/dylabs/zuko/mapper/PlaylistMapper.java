package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.PlaylistRequest;
import com.dylabs.zuko.dto.response.PlaylistResponse;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.model.Playlist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.SongRepository;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PlaylistMapper {

    private final SongRepository songRepository;
    private final UserRepository userRepository;

    public PlaylistResponse toResponse(Playlist playlist) {
        PlaylistResponse playlistResponse = new PlaylistResponse(
                playlist.getPlaylistId(),
                playlist.getName(),
                playlist.getDescription(),
                playlist.isPublic(),
                playlist.getCreatedAt(),
                playlist.getSongs().stream()
                        .map(this::toSongResponse)
                        .collect(Collectors.toSet()),
                playlist.getUrl_image(),
                playlist.getUser().getId()
        );
        return playlistResponse;
    }

    public Playlist toEntity(PlaylistRequest request) {
        Playlist playlist = new Playlist();
        playlist.setName(request.name());
        playlist.setDescription(request.description());
        playlist.setPublic(request.isPublic());
        playlist.setUrl_image(request.url_image());
        return playlist;
    }

    public SongResponse toSongResponse(Song song) {
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.isPublicSong(),
                song.getReleaseDate(),
                null,
                song.getArtist().getId(),
                song.getArtist().getName(),
                song.getYoutubeUrl(),
                song.getImageUrl()

        );
    }

    public Set<Song> idsToSongs(Set<Long> songIds) {
        if (songIds == null || songIds.isEmpty()) {
            return Set.of();
        }
        return songIds.stream()
                .map(songId -> songRepository.findById(songId)
                        .orElseThrow(() -> new SongNotFoundException("Canción no encontrada con ID: " + songId)))
                .collect(Collectors.toSet());
    }
}