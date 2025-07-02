package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.request.SongRequest;
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
import com.dylabs.zuko.repository.SongRepository;
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
    private final SongRepository songRepository;
    private final SongMapper songMapper;
    private final UserRepository userRepository;

    // Método reutilizable para validar que todas las canciones sean públicas
    private void validatePublicSongs(List<SongRequest> songs) {
        boolean allSongsArePublic = songs.stream().allMatch(SongRequest::isPublicSong);
        if (!allSongsArePublic) {
            throw new AlbumValidationException("Solo las canciones públicas pueden asociarse a álbumes.");
        }
    }

    public AlbumResponse createAlbum(AlbumRequest request, String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado"));

        Artist artist;
        if ("ADMIN".equalsIgnoreCase(user.getUserRoleName())) {
            if (request.artistId() == null) {
                throw new AlbumValidationException("El campo artistId es obligatorio para administradores.");
            }
            artist = artistRepository.findById(request.artistId())
                    .orElseThrow(() -> new ArtistNotFoundException("Artista no encontrado con ID: " + request.artistId()));
        } else {
            artist = artistRepository.findByUserId(user.getId())
                    .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));
        }

        Genre genre = genreRepository.findById(request.genreId())
                .orElseThrow(() -> new GenreNotFoundException("El género no fue encontrado."));

        if (albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        if (request.songs().size() < 2) {
            throw new AlbumValidationException("El álbum debe contener al menos dos canciones.");
        }

        // Validar que todas las canciones sean públicas
        validatePublicSongs(request.songs());

        List<Song> persistedSongs = request.songs().stream()
                .map(songRequest -> {
                    Song existingSong = songRepository.findByTitleContainingIgnoreCaseAndIsPublicSongTrue(songRequest.title())
                            .stream()
                            .filter(s -> s.getArtist().getId().equals(artist.getId()))
                            .findFirst()
                            .orElse(null);

                    if (existingSong == null) {
                        Song newSong = new Song();
                        newSong.setTitle(songRequest.title());
                        newSong.setPublicSong(songRequest.isPublicSong());
                        newSong.setReleaseDate(LocalDate.now());
                        newSong.setArtist(artist);
                        newSong.setImageUrl(songRequest.imageUrl());
                        newSong.setYoutubeUrl(songRequest.youtubeUrl());
                        return songRepository.save(newSong);
                    }

                    return existingSong;
                })
                .collect(Collectors.toList());

        Album album = albumMapper.toAlbumEntity(request, artist, genre);
        album.setSongs(persistedSongs);
        album.setReleaseDate(LocalDate.now());
        album = albumRepository.save(album);

        return albumMapper.toResponse(album);
    }





    public AlbumResponse getAlbumById(Long id) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));

        // Filtrar canciones públicas antes de devolver el álbum
        album.setSongs(
                album.getSongs().stream()
                        .filter(Song::isPublicSong) // Mantener solo canciones públicas
                        .collect(Collectors.toList())
        );

        return albumMapper.toResponse(album);
    }


    public List<AlbumResponse> getAlbumsByTitle(String title) {
        List<Album> albums = albumRepository.findAllByTitleContainingIgnoreCase(title);

        if (albums.isEmpty()) {
            throw new AlbumNotFoundException("No se encontraron álbumes con el título especificado.");
        }

        // Filtrar las canciones públicas en cada álbum
        albums.forEach(album ->
                album.setSongs(album.getSongs().stream()
                        .filter(Song::isPublicSong) // Mantener solo canciones públicas
                        .collect(Collectors.toList())
                )
        );

        return albums.stream().map(albumMapper::toResponse).collect(Collectors.toList());
    }


    public List<AlbumResponse> getAlbumsByTitleAndUser(String title, String userIdFromToken) {
        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado"));

        Artist artist = artistRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ArtistNotFoundException("No tienes un perfil de artista."));

        List<Album> albums = albumRepository.findAllByTitleContainingIgnoreCaseAndArtistId(title, artist.getId());
        if (albums.isEmpty()) {
            throw new AlbumNotFoundException("No se encontraron álbumes con el título especificado para este artista.");
        }
        return albums.stream().map(albumMapper::toResponse).collect(Collectors.toList());
    }

    public List<AlbumResponse> getAllAlbums() {
        List<Album> albums = albumRepository.findAll();
        if (albums.isEmpty()) {
            throw new AlbumNotFoundException("No se encontraron álbumes.");
        }
        return albums.stream().map(albumMapper::toResponse).collect(Collectors.toList());
    }

    public List<AlbumResponse> getAlbumsByArtistId(Long artistId) {
        if (!artistRepository.existsById(artistId)) {
            throw new ArtistNotFoundException("Artista no encontrado con ID: " + artistId);
        }

        List<Album> albums = albumRepository.findAllByArtistId(artistId);

        if (albums.isEmpty()) {
            throw new AlbumNotFoundException("No se encontraron álbumes para este artista.");
        }

        // Filtrar las canciones públicas en cada álbum
        albums.forEach(album ->
                album.setSongs(album.getSongs().stream()
                        .filter(Song::isPublicSong) // Mantener solo canciones públicas
                        .collect(Collectors.toList())
                )
        );

        return albums.stream().map(albumMapper::toResponse).collect(Collectors.toList());
    }




    public AlbumResponse updateAlbum(Long id, AlbumRequest request, String userIdFromToken) {
        Album album = albumRepository.findById(id)
                .orElseThrow(() -> new AlbumNotFoundException("Álbum no disponible."));

        User user = userRepository.findById(Long.parseLong(userIdFromToken))
                .orElseThrow(() -> new ArtistNotFoundException("Usuario no encontrado"));

        Artist artist;
        if ("ADMIN".equalsIgnoreCase(user.getUserRoleName()) && request.artistId() != null) {
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

        if (albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(request.title(), artist.getId(), id)) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        if (request.songs().size() < 2) {
            throw new AlbumValidationException("El álbum debe contener al menos dos canciones.");
        }

        // Validar que todas las canciones sean públicas
        validatePublicSongs(request.songs());

        List<Song> persistedSongs = request.songs().stream()
                .map(songRequest -> {

                    Song existingSong = songRepository.findByTitleContainingIgnoreCaseAndIsPublicSongTrue(songRequest.title())
                            .stream()
                            .filter(s -> s.getArtist().getId().equals(artist.getId()))
                            .findFirst()
                            .orElse(null);

                    if (existingSong == null) {
                        Song newSong = new Song();
                        newSong.setTitle(songRequest.title());
                        newSong.setPublicSong(songRequest.isPublicSong());
                        newSong.setReleaseDate(LocalDate.now());
                        newSong.setArtist(artist);
                        newSong.setImageUrl(songRequest.imageUrl());
                        newSong.setYoutubeUrl(songRequest.youtubeUrl());
                        return songRepository.save(newSong);
                    }

                    return existingSong;
                })
                .collect(Collectors.toList());

        albumMapper.updateAlbumFromRequest(album, request, genre, artist);
        album.setSongs(persistedSongs);
        album = albumRepository.save(album);

        return albumMapper.toResponse(album);
    }




    public void deleteAlbum(Long id, String userIdFromToken) {
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




    public AlbumResponse getAlbumBySongId(Long songId) {
        Album album = albumRepository.findAlbumBySongId(songId)
                .orElseThrow(() -> new AlbumNotFoundException("No se encontró un álbum para esta canción"));
        return albumMapper.toResponse(album);
    }


}