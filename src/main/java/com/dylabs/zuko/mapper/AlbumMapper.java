package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.dto.response.AlbumSongSummaryResponse;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.model.Song;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class AlbumMapper {

    public Album toAlbumEntity(AlbumRequest request, Artist artist, Genre genre) {
        Album album = new Album();
        album.setTitle(request.title());
        album.setReleaseYear(request.releaseYear());
        album.setCover(request.cover());
        album.setArtist(artist);
        album.setGenre(genre);
        album.setCreationDate(LocalDate.now());

        // Mapear canciones si existen
        if (request.songs() != null) {
            List<Song> songs = request.songs().stream()
                    .map(this::mapToSong)
                    .collect(Collectors.toList());
            album.setSongs(songs);
        } else {
            album.setSongs(List.of()); // Si no se reciben canciones, se asigna una lista vacía
        }

        return album;
    }

    private Song mapToSong(SongRequest songRequest) {
        Song song = new Song();
        song.setTitle(songRequest.title());
        song.setPublicSong(songRequest.isPublicSong());
        song.setReleaseDate(LocalDate.now());
        return song;
    }

    public AlbumResponse toResponse(Album album) {
        List<AlbumSongSummaryResponse> songs = album.getSongs().stream()
                .map(song -> new AlbumSongSummaryResponse(song.getTitle()))
                .toList();

        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                album.getArtist().getName(),
                album.getGenre().getName(),
                songs
        );
    }

}
