package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.albumExceptions.AlbumPermissionException;
import com.dylabs.zuko.exception.albumExceptions.AlbumValidationException;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.mapper.AlbumMapper;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.AlbumRepository;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.GenreRepository;
import com.dylabs.zuko.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final UserRepository userRepository;

    public AlbumResponse createAlbum(AlbumRequest request, String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado"));

        Artist artist;
        if ("ADMIN".equalsIgnoreCase(user.getUserRoleName())) {
            artist = artistRepository.findById(request.artistId())
                    .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + request.artistId()));
        } else {
            artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));
            if (!artist.getId().equals(request.artistId())) {
                throw new AlbumPermissionException("No puedes crear un álbum para otro artista.");
            }
        }

        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        if (albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        if (request.songs() == null || request.songs().size() < 2) {
            throw new AlbumValidationException("El álbum debe contener al menos dos canciones.");
        }

        Album album = albumMapper.toAlbumEntity(request, artist, genre);
        album.setReleaseDate(LocalDate.now());
        album.setCreationDate(LocalDate.now());
        albumRepository.save(album);

        return albumMapper.toResponse(album);
    }


    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));
        return albumMapper.toResponse(album);
    }


    public AlbumResponse updateAlbum(Long id, AlbumRequest request, String userIdFromToken) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado"));

        Artist artist;
        if ("ADMIN".equalsIgnoreCase(user.getUserRoleName())) {
            artist = artistRepository.findById(request.artistId())
                    .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + request.artistId()));
        } else {
            artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));
            if (!album.getArtist().getId().equals(artist.getId())) {
                throw new AccessDeniedException("No puedes modificar este álbum.");
            }
        }

        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        boolean existsWithSameTitle = albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(
                request.title(), artist.getId(), album.getId());
        if (existsWithSameTitle) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        if (request.songs() == null || request.songs().size() < 2) {
            throw new AlbumValidationException("El álbum debe contener al menos dos canciones.");
        }

        albumMapper.updateAlbumFromRequest(album, request, genre, artist);
        albumRepository.save(album);

        return albumMapper.toResponse(album);
    }



    public void deleteAlbum(Long id, String userIdFromToken) {
        // Buscar el álbum por ID
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));

        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado"));

        if (!"ADMIN".equalsIgnoreCase(user.getUserRoleName())) {
            Artist artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));

            if (!album.getArtist().getId().equals(artist.getId())) {

                throw new AlbumPermissionException("No puedes eliminar este álbum.");
            }
        }

        albumRepository.delete(album);
    }

}