package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.dto.response.AlbumSongSummaryResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.mapper.AlbumMapper;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.repository.AlbumRepository;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final AlbumMapper albumMapper;

    public AlbumResponse createAlbum(AlbumRequest request) {
        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new ArtistNotFoundException("El artista no fue encontrado."));

        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        boolean exists = albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId());
        if (exists) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        Album album = albumMapper.toAlbumEntity(request, artist, genre);
        album.setReleaseDate(LocalDate.now());
        album.setCreationDate(LocalDate.now());

        if (album.getSongs() == null || album.getSongs().size() < 2) {
            throw new IllegalArgumentException("El álbum debe contener al menos dos canciones.");
        }

        album.getSongs().forEach(song -> song.setArtist(artist));
        albumRepository.save(album);

        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                artist.getName(),
                genre.getName(),
                album.getSongs().stream()
                        .map(song -> new AlbumSongSummaryResponse(song.getTitle()))
                        .collect(Collectors.toList())
        );
    }

    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));
                //.orElseThrow(() -> new AlbumNotFoundException("Álbum no encontrado con ID: " + id));

        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                album.getArtist().getName(),
                album.getGenre().getName(),
                album.getSongs().stream()
                        .map(song -> new AlbumSongSummaryResponse(song.getTitle()))
                        .collect(Collectors.toList())
        );
    }
}
