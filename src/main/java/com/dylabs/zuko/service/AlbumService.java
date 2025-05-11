package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
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

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final AlbumMapper albumMapper;

    public AlbumResponse createAlbum(AlbumRequest request) {
        // Obtener artista (por ahora, asumimos un artista específico ya que aún no hay autenticación)
        Artist artist = artistRepository.findById(1L)
                .orElseThrow(() -> new ArtistNotFoundException("El artista no fue encontrado."));

        // Verificar existencia del género
        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        // Verificar duplicado por título y artista
        boolean exists = albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId());
        if (exists) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        // Crear entidad Album (no verificamos relación de canciones con artista por ahora)
        Album album = albumMapper.toAlbumEntity(request, artist, genre);
        album.setReleaseDate(LocalDate.now());

        albumRepository.save(album);

        return albumMapper.toResponse(album);
    }
}
