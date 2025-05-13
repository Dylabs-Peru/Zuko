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

    // Mapear un DTO de solicitud de álbum a entidad de álbum
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
                    .map(songRequest -> mapToSong(songRequest, artist)) // Usar el artista
                    .collect(Collectors.toList());
            album.setSongs(songs);
        } else {
            album.setSongs(List.of()); // Si no se reciben canciones, se asigna una lista vacía
        }

        return album;
    }

    // Mapear una canción a entidad Song
    private Song mapToSong(SongRequest songRequest, Artist artist) {
        Song song = new Song();
        song.setTitle(songRequest.title());
        song.setPublicSong(songRequest.isPublicSong());
        song.setReleaseDate(LocalDate.now());
        song.setArtist(artist); // Asignar artista
        return song;
    }

    // Convertir entidad de álbum a DTO de respuesta
    public AlbumResponse toResponse(Album album) {
        List<AlbumSongSummaryResponse> songs = album.getSongs().stream()
                .map(song -> new AlbumSongSummaryResponse(song.getTitle()))
                .collect(Collectors.toList());

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

    // Método para actualizar un álbum sin reemplazar la lista completa de canciones
    // Ahora acepta el parámetro `Artist`
    public void updateAlbumFromRequest(Album album, AlbumRequest request, Genre genre, Artist artist) {
        album.setTitle(request.title());
        album.setReleaseYear(request.releaseYear());
        album.setCover(request.cover());
        album.setGenre(genre);

        // Actualizar canciones sin reemplazar la lista completa
        List<Song> updatedSongs = request.songs().stream()
                .map(songRequest -> mapToSong(songRequest, artist)) // Usar el artista aquí
                .collect(Collectors.toList());

        // Limpiar las canciones previas y agregar las nuevas
        album.getSongs().clear();           // Evitar problemas con orphanRemoval
        album.getSongs().addAll(updatedSongs); // Asignar las canciones actualizadas
    }
}
