package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.mapper.AlbumMapper;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.AlbumRepository;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final GenreRepository genreRepository;
    private final AlbumMapper albumMapper;
    private final SongMapper songMapper;

    // Metodo para crear un álbum
    public AlbumResponse createAlbum(AlbumRequest request) {
        // Buscar el artista y el género
        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new ArtistNotFoundException("El artista no fue encontrado."));
        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        // Verificar si el álbum ya existe para ese artista
        boolean exists = albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId());
        if (exists) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        // Validar que el álbum contenga al menos dos canciones
        if (request.songs() == null || request.songs().size() < 2) {
            throw new IllegalArgumentException("El álbum debe contener al menos dos canciones.");
        }

        // Convertir el DTO de solicitud a entidad de álbum
        Album album = albumMapper.toAlbumEntity(request, artist, genre);

        // Asignar las canciones al álbum
        List<Song> songList = request.songs().stream()
                .map(songRequest -> songMapper.toSongEntity(songRequest, artist)) // Aquí se usa el artista
                .collect(Collectors.toList());
        album.setSongs(songList);

        // Establecer fechas
        album.setReleaseDate(LocalDate.now());
        album.setCreationDate(LocalDate.now());

        // Guardar el álbum en la base de datos
        albumRepository.save(album);

        return albumMapper.toResponse(album);
    }

    // Método para obtener un álbum por su ID
    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));
        return albumMapper.toResponse(album);
    }

    // Metodo para actualizar un álbum existente
    public AlbumResponse updateAlbum(Long id, AlbumRequest request) {
        // Buscar el álbum y el artista
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));
        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new ArtistNotFoundException("El artista no fue encontrado."));

        // Verificar que el artista que intenta editar el álbum sea el mismo que el creador
        if (!album.getArtist().getId().equals(artist.getId())) {
            throw new IllegalArgumentException("No tienes permiso para editar este álbum.");
        }

        // Buscar el género
        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        // Validar que el álbum contenga al menos dos canciones
        if (request.songs() == null || request.songs().size() < 2) {
            throw new IllegalArgumentException("El álbum debe contener al menos dos canciones.");
        }

        boolean existsWithSameTitle = albumRepository
                .existsByTitleIgnoreCaseAndArtistIdAndIdNot(request.title(), artist.getId(), album.getId());

        if (existsWithSameTitle) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        // Usar el mapper para actualizar los campos del álbum y las canciones
        albumMapper.updateAlbumFromRequest(album, request, genre, artist); // Pasar el artista al mapper

        // Guardar el álbum actualizado en la base de datos
        albumRepository.save(album);

        return albumMapper.toResponse(album);
    }

    // Metodo para eliminar un álbum por su ID
    public void deleteAlbum(Long id, Long artistId) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ArtistNotFoundException("El artista no fue encontrado."));

        if (!album.getArtist().getId().equals(artist.getId())) {
            throw new IllegalArgumentException("No tienes permiso para eliminar este álbum.");
        }

        albumRepository.delete(album);
    }
}
