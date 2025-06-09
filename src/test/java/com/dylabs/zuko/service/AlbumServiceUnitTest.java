package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.dto.response.AlbumSongSummaryResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.albumExceptions.AlbumPermissionException;
import com.dylabs.zuko.exception.albumExceptions.AlbumValidationException;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Optional;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.any;


public class AlbumServiceUnitTest {

    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private AlbumMapper albumMapper;
    @Mock
    private SongMapper songMapper;
    @Mock
    private UserRepository userRepository;
    @Mock
    private ArtistRepository artistRepository;
    @Mock
    private SongRepository songRepository;
    @Mock
    private GenreRepository genreRepository;

    @InjectMocks
    private AlbumService albumService;

    private Artist artist;
    private Genre genre;
    private Song song1;
    private Song song2;
    private User ownerUser;
    private User otherUser;
    private User adminUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        artist = new Artist();
        artist.setId(1L);
        artist.setName("Artista Test");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Pop");

        song1 = new Song();
        song1.setId(1L);
        song1.setTitle("Canción 1");
        song2 = new Song();
        song2.setId(2L);
        song2.setTitle("Canción 2");


        ownerUser = new User();
        ownerUser.setId(10L);
        ownerUser.setUsername("usuarioDueno");
        ownerUser.setUserRoleName("OWNER");

        otherUser = new User();
        otherUser.setId(20L);
        otherUser.setUsername("usuarioOtro");
        otherUser.setUserRoleName("USER");

        adminUser = new User();
        adminUser.setId(30L);
        adminUser.setUsername("usuarioAdmin");
        adminUser.setUserRoleName("ADMIN");
    }


    @Test
    @DisplayName("CP01 - HU10 Registro exitoso del álbum con todos los datos válidos")
    void createAlbum_withValidData_returnsAlbumSuccessfully() {

        String albumTitle = "Mi Primer Álbum";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();

        SongRequest songRequest1 = new SongRequest(song1.getTitle(), song1.isPublicSong(), artistId);
        SongRequest songRequest2 = new SongRequest(song2.getTitle(), song2.isPublicSong(), artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);

        AlbumRequest request = new AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        Album album = new Album();
        album.setId(10L);
        album.setTitle(albumTitle);
        album.setReleaseYear(year);
        album.setCover(cover);
        album.setGenre(genre);
        album.setArtist(artist);
        album.setSongs(List.of(song1, song2));

        List<AlbumSongSummaryResponse> songSummaries = List.of(
                new AlbumSongSummaryResponse(song1.getTitle()),
                new AlbumSongSummaryResponse(song2.getTitle())
        );
        AlbumResponse expectedResponse = new AlbumResponse(
                album.getId(),
                albumTitle,
                year,
                cover,
                artist.getId(),
                artist.getName(),
                genre.getName(),
                songSummaries
        );

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(albumTitle, artistId)).thenReturn(false);
        when(songMapper.toSongEntity(songRequest1, artist)).thenReturn(song1);
        when(songMapper.toSongEntity(songRequest2, artist)).thenReturn(song2);
        when(albumMapper.toAlbumEntity(request, artist, genre)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toResponse(album)).thenReturn(expectedResponse);

        AlbumResponse response = albumService.createAlbum(request);

        assertEquals(albumTitle, response.title());
        assertEquals(year, response.releaseYear());
        assertEquals(cover, response.cover());
        assertEquals(artist.getName(), response.artistName());
        assertEquals(genre.getName(), response.genreName());
        assertEquals(songSummaries, response.songs());
        verify(albumRepository).save(any(Album.class));
    }

    @Test
    @DisplayName("CP02 - HU10 Registro fallido por menos de dos canciones")
    void createAlbum_withOneSong_throwsAtLeastTwoSongsException() {

        String albumTitle = "Álbum con una canción";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        SongRequest songRequest1 = new SongRequest("Solo canción", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1);

        AlbumRequest request = new AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

        assertThrows(
                AlbumValidationException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción de validación porque el álbum tiene menos de dos canciones"
        );
    }

    @Test
    @DisplayName("CP03 - HU10 Registro fallido al no tener canciones")
    void createAlbum_withNullSongs_throwsAtLeastTwoSongsException() {

        String albumTitle = "Álbum sin canciones";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        AlbumRequest request = new AlbumRequest(
                albumTitle, year, cover, artistId, genreId, null
        );

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

        assertThrows(
                AlbumValidationException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción de validación porque la lista de canciones es nula"
        );
    }

    @Test
    @DisplayName("CP04 - HU10 Registro fallido por campos obligatorios vacíos")
    void createAlbum_withMissingTitleOrYear_throwsValidationException() {

        String albumTitle = null;
        int year = 2024;
        String cover = null;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        SongRequest songRequest2 = new SongRequest("Canción 2", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);
        AlbumRequest request = new AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumMapper.toAlbumEntity(
                argThat(req -> req.title() == null),
                any(),
                any()
        )).thenThrow(new AlbumValidationException("El título es obligatorio"));

        assertThrows(
                AlbumValidationException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción de validación por campos obligatorios faltantes"
        );
    }

    @Test
    @DisplayName("CP05 - HU10 Registro fallido por título duplicado")
    void createAlbum_withDuplicateTitle_throwsDuplicateTitleException() {

        String albumTitle = "Álbum Duplicado";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        SongRequest songRequest2 = new SongRequest("Canción 2", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);

        AlbumRequest request = new AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(albumTitle, artistId)).thenReturn(true);

        assertThrows(
                AlbumAlreadyExistsException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción por título duplicado de álbum para el artista"
        );
    }




    @Test
    @DisplayName("CP01 - HU11 Visualización exitosa de un álbum existente")
    void getAlbumById_withValidId_returnsAlbumDetails() {

        Long albumId = 10L;
        String albumTitle = "Álbum Detalle";
        int year = 2024;
        String cover = "cover.jpg";
        String artistName = artist.getName();
        String genreName = genre.getName();
        List<AlbumSongSummaryResponse> songSummaries = List.of(
                new AlbumSongSummaryResponse("Canción 1"),
                new AlbumSongSummaryResponse("Canción 2")
        );

        Album album = new Album();
        album.setId(albumId);
        album.setTitle(albumTitle);
        album.setReleaseYear(year);
        album.setCover(cover);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setSongs(List.of(song1, song2));

        AlbumResponse expectedResponse = new AlbumResponse(
                albumId,
                albumTitle,
                year,
                cover,
                artist.getId(),
                artistName,
                genreName,
                songSummaries
        );

        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumMapper.toResponse(album)).thenReturn(expectedResponse);

        AlbumResponse response = albumService.getAlbumById(albumId);

        assertEquals(albumId, response.id());
        assertEquals(albumTitle, response.title());
        assertEquals(year, response.releaseYear());
        assertEquals(cover, response.cover());
        assertEquals(artist.getId(), response.artistId());
        assertEquals(artistName, response.artistName());
        assertEquals(genreName, response.genreName());
        assertEquals(songSummaries, response.songs());
    }

    @Test
    @DisplayName("CP02 - HU11 Álbum no encontrado")
    void getAlbumById_withInvalidId_throwsAlbumNotFoundException() {

        Long invalidAlbumId = 99L;
        when(albumRepository.findById(invalidAlbumId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(
                AlbumNotFoundException.class,
                () -> albumService.getAlbumById(invalidAlbumId),
                "Se esperaba excepción por álbum no encontrado"
        );
        assertEquals("Álbum no disponible.", ex.getMessage());
    }




    @Test
    @DisplayName("CP01 - HU12 Edición exitosa con datos válidos")
    void updateAlbum_withValidData_updatesSuccessfully() {

        Long albumId = 1L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        String newTitle = "Nuevo Álbum";
        int newYear = 2025;
        String newCover = "nuevo-cover.jpg";
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        SongRequest songRequest2 = new SongRequest("Canción 2", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);
        AlbumRequest request = new AlbumRequest(
                newTitle, newYear, newCover, artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        artist.setUser(ownerUser);
        album.setArtist(artist);
        album.setGenre(genre);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));
        doNothing().when(albumMapper).updateAlbumFromRequest(any(Album.class), any(), any(), any());
        AlbumResponse expectedResponse = new AlbumResponse(
                albumId,
                newTitle,
                newYear,
                newCover,
                artistId,
                artist.getName(),
                genre.getName(),
                List.of(
                        new AlbumSongSummaryResponse("Canción 1"),
                        new AlbumSongSummaryResponse("Canción 2")
                )
        );
        when(albumMapper.toResponse(any(Album.class))).thenReturn(expectedResponse);
        AlbumResponse response = albumService.updateAlbum(albumId, request, ownerUser.getUsername());

        assertEquals(newTitle, response.title());
        assertEquals(newYear, response.releaseYear());
        assertEquals(newCover, response.cover());
        assertEquals(artist.getName(), response.artistName());
        assertEquals(genre.getName(), response.genreName());
        verify(albumRepository).save(album);
    }

    @Test
    @DisplayName("CP02 - HU12 Edición rechazada por menos de dos canciones")
    void updateAlbum_withOneSong_throwsValidationException() {

        Long albumId = 2L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1);
        AlbumRequest request = new AlbumRequest(
                "Álbum Test", 2025, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);

        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));

        Exception ex = assertThrows(
                AlbumValidationException.class,
                () -> albumService.updateAlbum(albumId, request, ownerUser.getUsername()),
                "Se esperaba excepción por intentar dejar solo una canción"
        );
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    @Test
    @DisplayName("CP03 - HU12 Edición rechazada sin canciones")
    void updateAlbum_withNullSongs_throwsValidationException() {

        Long albumId = 3L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        AlbumRequest request = new AlbumRequest(
                "Álbum sin canciones", 2025, "cover.jpg", artistId, genreId, null
        );
        Album album = new Album();
        album.setId(albumId);
        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));

        Exception ex = assertThrows(
                AlbumValidationException.class,
                () -> albumService.updateAlbum(albumId, request, ownerUser.getUsername()),
                "Se esperaba excepción por lista de canciones nula"
        );
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    @Test
    @DisplayName("CP04 - HU12 Edición fallida por campos vacíos o inválidos")
    void updateAlbum_withMissingRequiredFields_throwsValidationException() {

        Long albumId = 4L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1);
        AlbumRequest request = new AlbumRequest(
                "Álbum sin canciones", 2025, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));

        Exception ex = assertThrows(
                AlbumValidationException.class,
                () -> albumService.updateAlbum(albumId, request, ownerUser.getUsername()),
                "Se esperaba excepción por campo obligatorio faltante"
        );
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    @Test
    @DisplayName("CP05 - HU12 Edición rechazada por artista no autorizado")
    void updateAlbum_whenUserIsNotOwner_throwsPermissionException() {

        Long albumId = 5L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        SongRequest songRequest2 = new SongRequest("Canción 2", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);
        AlbumRequest request = new AlbumRequest(
                "Álbum ajeno", 2025, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);

        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(userRepository.findByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));

        Exception ex = assertThrows(
                AlbumPermissionException.class,
                () -> albumService.updateAlbum(albumId, request, otherUser.getUsername()),
                "Se esperaba excepción por falta de permisos"
        );
        assertEquals("No tienes permisos para editar este álbum", ex.getMessage());
    }

    @Test
    @DisplayName("CP06 - HU12 Edición rechazada por título ya registrado")
    void updateAlbum_withDuplicateTitle_throwsDuplicateTitleException() {

        Long albumId = 6L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        String duplicateTitle = "Álbum Duplicado";
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        SongRequest songRequest2 = new SongRequest("Canción 2", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);
        AlbumRequest request = new AlbumRequest(
                duplicateTitle, 2025, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);

        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(duplicateTitle, artistId, albumId)).thenReturn(true);
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));
        // Act & Assert
        Exception ex = assertThrows(
                AlbumAlreadyExistsException.class,
                () -> albumService.updateAlbum(albumId, request, ownerUser.getUsername()),
                "Se esperaba excepción por título duplicado de álbum para el artista"
        );
        assertEquals("El título del álbum ya existe para este artista.", ex.getMessage());
    }

    //SECURITY
    @Test
    @DisplayName("CP07 - HU12 Edición exitosa por usuario ADMIN no dueño")
    void updateAlbum_withAdminUser_updatesSuccessfully() {

        Long albumId = 200L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        SongRequest songRequest1 = new SongRequest("Canción 1", true, artistId);
        SongRequest songRequest2 = new SongRequest("Canción 2", true, artistId);
        List<SongRequest> songRequests = List.of(songRequest1, songRequest2);
        AlbumRequest request = new AlbumRequest(
                "Álbum editado por admin", 2026, "coverAdmin.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);

        User notAdminOwner = new User();
        notAdminOwner.setId(99L);
        notAdminOwner.setUsername("otroUsuario");
        notAdminOwner.setUserRoleName("OWNER");
        artist.setUser(notAdminOwner);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));
        when(albumMapper.toResponse(any(Album.class))).thenReturn(mock(AlbumResponse.class));

        albumService.updateAlbum(albumId, request, adminUser.getUsername());

        verify(albumRepository).save(album);
    }
    //SECURITY




    @Test
    @DisplayName("CP01 - HU27 Eliminación exitosa de un álbum propio")
    void deleteAlbum_withValidOwner_deletesSuccessfully() {

        Long albumId = 100L;
        Long artistId = artist.getId();
        Album album = new Album();
        album.setId(albumId);

        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));

        albumService.deleteAlbum(albumId, ownerUser.getUsername());

        verify(albumRepository).delete(album);
    }

    @Test
    @DisplayName("CP02 - HU27 Eliminación rechazada por falta de autorización")
    void deleteAlbum_whenUserIsNotOwner_throwsPermissionException() {

        Long albumId = 101L;
        Long artistId = artist.getId();
        Artist anotherArtist = new Artist();
        anotherArtist.setId(2L);
        Album album = new Album();
        album.setId(albumId);

        artist.setUser(ownerUser);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(2L)).thenReturn(Optional.of(anotherArtist));
        when(userRepository.findByUsername(otherUser.getUsername())).thenReturn(Optional.of(otherUser));

        Exception ex = assertThrows(
                AlbumPermissionException.class,
                () -> albumService.deleteAlbum(albumId, otherUser.getUsername()),
                "Se esperaba excepción por falta de permisos al eliminar álbum ajeno"
        );
        assertEquals("No tienes permiso para eliminar este álbum.", ex.getMessage());
        verify(albumRepository, never()).delete(any());
    }

    @Test
    @DisplayName("CP03 - HU27 Eliminación fallida por álbum inexistente")
    void deleteAlbum_withInvalidAlbumId_throwsNotFoundException() {

        Long albumId = 999L;
        Long artistId = artist.getId();
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());
        when(userRepository.findByUsername(ownerUser.getUsername())).thenReturn(Optional.of(ownerUser));

        Exception ex = assertThrows(
                AlbumNotFoundException.class,
                () -> albumService.deleteAlbum(albumId, ownerUser.getUsername()),
                "Se esperaba excepción por álbum no encontrado al eliminar"
        );
        assertEquals("Álbum no disponible.", ex.getMessage());
        verify(albumRepository, never()).delete(any());
    }

    //SECURITY
    @Test
    @DisplayName("CP04 - HU27 Eliminación exitosa por usuario ADMIN no dueño")
    void deleteAlbum_withAdminUser_deletesSuccessfully() {

        Long albumId = 201L;
        Long artistId = artist.getId();
        Album album = new Album();
        album.setId(albumId);

        User notAdminOwner = new User();
        notAdminOwner.setId(99L);
        notAdminOwner.setUsername("otroUsuario");
        notAdminOwner.setUserRoleName("OWNER");
        artist.setUser(notAdminOwner);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(userRepository.findByUsername(adminUser.getUsername())).thenReturn(Optional.of(adminUser));

        albumService.deleteAlbum(albumId, adminUser.getUsername());

        verify(albumRepository).delete(album);
    }
    //SECURITY
}