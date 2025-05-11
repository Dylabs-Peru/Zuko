package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
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
        album.setArtist(artist);                  // Asociar artista al álbum
        album.setGenre(genre);                    // Asociar género
        album.setCreationDate(LocalDate.now());   // Fecha automática

        // Mapear canciones desde el request
        List<Song> songs = request.songs().stream()
                .map(this::mapToSong)
                .collect(Collectors.toList());
        album.setSongs(songs);

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
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                album.getArtist().getId(),     // Retornar el ID del artista
                album.getGenre().getName()
        );
    }
}
