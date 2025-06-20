package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.*;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.mapper.AlbumMapper;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

class AlbumServiceUnitTest {

    @Mock private AlbumRepository albumRepository;
    @Mock private AlbumMapper albumMapper;
    @Mock private UserRepository userRepository;
    @Mock private ArtistRepository artistRepository;
    @Mock private GenreRepository genreRepository;
    @Mock private SongRepository songRepository;

    @InjectMocks private AlbumService albumService;

    private Artist artist;
    private Genre genre;
    private User ownerUser;
    private User adminUser;
    private Album album;
    private AlbumRequest validRequest;
    private SongRequest validSongRequest;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        artist = new Artist();
        artist.setId(1L);
        artist.setName("Artista Test");

        genre = new Genre();
        genre.setId(1L);
        genre.setName("Pop");

        ownerUser = new User();
        ownerUser.setId(10L);
        ownerUser.setUsername("usuarioDueno");
        ownerUser.setUserRoleName("OWNER");

        adminUser = new User();
        adminUser.setId(99L);
        adminUser.setUsername("admin");
        adminUser.setUserRoleName("ADMIN");

        album = new Album();
        album.setId(1L);
        album.setTitle("Álbum Existente");
        album.setArtist(artist);
        album.setGenre(genre);

        validSongRequest = new SongRequest("Canción válida", true, artist.getId());

        validRequest = new AlbumRequest(
                "Nuevo Álbum", 2023, "cover.jpg", artist.getId(), genre.getId(),
                List.of(
                        new SongRequest("Canción 1", true, artist.getId()),
                        new SongRequest("Canción 2", true, artist.getId())
                )
        );

        when(songRepository.existsByTitleIgnoreCaseAndArtistId(anyString(), anyLong())).thenReturn(true);
        when(songRepository.findAll()).thenReturn(List.of(
                // Canciones existentes simuladas
                getMockSong("Canción 1", artist),
                getMockSong("Canción 2", artist),
                getMockSong("Canción válida", artist)
        ));
    }

    private Song getMockSong(String title, Artist artist) {
        Song song = new Song();
        song.setTitle(title);
        song.setArtist(artist);
        return song;
    }

    @Test
    @DisplayName("CP01 - HU10 Registro exitoso del álbum con todos los datos válidos")
    void createAlbum_withValidData_successfullyCreatesAlbum() {

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(validRequest.title(), artist.getId())).thenReturn(false);
        when(albumMapper.toAlbumEntity(validRequest, artist, genre)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);

        AlbumResponse expectedResponse = mock(AlbumResponse.class);
        when(albumMapper.toResponse(album)).thenReturn(expectedResponse);

        AlbumResponse result = albumService.createAlbum(validRequest, ownerUser.getId().toString());

        assertEquals(expectedResponse, result);
        verify(albumRepository).save(album);
    }


    @Test
    @DisplayName("CP02 - HU10 Registro exitoso del álbum como ADMIN")
    void createAlbum_asAdmin_success() {

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(artistRepository.findById(validRequest.artistId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(validRequest.title(), artist.getId())).thenReturn(false);
        when(albumMapper.toAlbumEntity(validRequest, artist, genre)).thenReturn(album);
        when(albumRepository.save(album)).thenReturn(album);
        when(albumMapper.toResponse(album)).thenReturn(mock(AlbumResponse.class));

        assertDoesNotThrow(() -> albumService.createAlbum(validRequest, adminUser.getId().toString()));
    }

    @Test
    @DisplayName("CP03 - HU10 Registro fallido de álbum por género inválido")
    void createAlbum_withInvalidGenre_throwsGenreNotFoundException() {
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.empty());

        assertThrows(GenreNotFoundException.class,
                () -> albumService.createAlbum(validRequest, ownerUser.getId().toString()));
    }

    @Test
    @DisplayName("CP04 - HU10 Registro fallido por título duplicado")
    void createAlbum_withDuplicateTitle_throwsException() {
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(validRequest.title(), artist.getId())).thenReturn(true);

        assertThrows(AlbumAlreadyExistsException.class,
                () -> albumService.createAlbum(validRequest, ownerUser.getId().toString()));
    }

    @Test
    @DisplayName("CP05 - HU10 Registro fallido al no tener canciones")
    void createAlbum_withNullSongs_throwsAlbumValidationException() {

        AlbumRequest requestConSongsNulo = new AlbumRequest(
                validRequest.title(),
                validRequest.releaseYear(),
                validRequest.cover(),
                validRequest.artistId(),
                validRequest.genreId(),
                null
        );

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(anyString(), anyLong())).thenReturn(false);

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.createAlbum(requestConSongsNulo, ownerUser.getId().toString()));
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    @Test
    @DisplayName("CP06 - HU10 Registro fallido por menos de dos canciones")
    void createAlbum_withLessThanTwoSongs_throwsAlbumValidationException() {
        AlbumRequest invalidRequest = new AlbumRequest(
                "Álbum", 2023, "cover.jpg", artist.getId(), genre.getId(),
                List.of(new SongRequest("Una sola", true, artist.getId()))
        );

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(invalidRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(invalidRequest.title(), artist.getId())).thenReturn(false);

        assertThrows(AlbumValidationException.class,
                () -> albumService.createAlbum(invalidRequest, ownerUser.getId().toString()));
    }


    @Test
    @DisplayName("CP07 - HU10 Registro fallido por intentar crear álbum como artista no autorizado")
    void createAlbum_ownerCreatesAlbumForAnotherArtist_throwsPermissionException() {
        AlbumRequest otherArtistRequest = new AlbumRequest(
                "Título", 2023, "img.jpg", 999L, genre.getId(),
                List.of(new SongRequest("Canción 1", true, 999L), new SongRequest("Canción 2", true, 999L))
        );

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));

        assertThrows(AlbumPermissionException.class,
                () -> albumService.createAlbum(otherArtistRequest, ownerUser.getId().toString()));
    }

    @Test
    @DisplayName("CP08 - HU10 Registro fallido por ingresar una canción no existente para el artista")
    void createAlbum_fails_whenSongDoesNotExistForArtist() {

        AlbumRequest request = new AlbumRequest(
                "Álbum con canción inexistente", 2023, "cover.jpg", artist.getId(), genre.getId(),
                List.of(new SongRequest("Canción Inexistente", true, artist.getId()), validSongRequest)
        );

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(request.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())).thenReturn(false);

        when(songRepository.existsByTitleIgnoreCaseAndArtistId(eq("Canción Inexistente"), eq(artist.getId()))).thenReturn(false);
        when(songRepository.existsByTitleIgnoreCaseAndArtistId(eq(validSongRequest.title()), eq(artist.getId()))).thenReturn(true);

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.createAlbum(request, ownerUser.getId().toString()));
        assertTrue(ex.getMessage().contains("no existe para este artista"));
    }

    @Test
    @DisplayName("CP09 - HU10 Registro fallido por ingresar una canción no perteneciente al artista")
    void createAlbum_fails_whenSongNotBelongsToArtist() {

        AlbumRequest request = new AlbumRequest(
                "Álbum con canción de otro artista", 2023, "cover.jpg", artist.getId(), genre.getId(),
                List.of(validSongRequest, new SongRequest("Ajena", true, 999L))
        );

        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(request.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())).thenReturn(false);

        when(songRepository.existsByTitleIgnoreCaseAndArtistId(anyString(), eq(artist.getId()))).thenReturn(true);
        when(songRepository.findAll()).thenReturn(List.of(
                getMockSong(validSongRequest.title(), artist)
        ));

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.createAlbum(request, ownerUser.getId().toString()));
        assertTrue(ex.getMessage().contains("no pertenece al artista"));
    }




    @Test
    @DisplayName("CP01 - HU11: Obtener álbum por ID exitoso")
    void getAlbumById_returnsAlbumResponse() {
        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        AlbumResponse expected = mock(AlbumResponse.class);
        when(albumMapper.toResponse(album)).thenReturn(expected);

        assertEquals(expected, albumService.getAlbumById(album.getId()));
    }

    @Test
    @DisplayName("CP02 - HU11: Obtener álbum por ID inexistente")
    void getAlbumById_albumNotFound_throwsException() {
        when(albumRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(AlbumNotFoundException.class,
                () -> albumService.getAlbumById(999L));
    }





    @Test
    @DisplayName("CP01 - HU12: Edicion exitosa de álbum como ADMIN")
    void updateAlbum_asAdmin_success() {

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(artistRepository.findById(validRequest.artistId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(validRequest.title(), artist.getId(), album.getId())).thenReturn(false);

        when(albumMapper.toResponse(album)).thenReturn(mock(AlbumResponse.class));

        assertDoesNotThrow(() -> albumService.updateAlbum(album.getId(), validRequest, adminUser.getId().toString()));
    }

    @Test
    @DisplayName("CP02 - HU12: Edición rechazada por título ya registrado")
    void updateAlbum_withDuplicateTitle_throwsException() {
        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(validRequest.title(), artist.getId(), album.getId()))
                .thenReturn(true);

        assertThrows(AlbumAlreadyExistsException.class,
                () -> albumService.updateAlbum(album.getId(), validRequest, ownerUser.getId().toString()));
    }

    @Test
    @DisplayName("CP03 - HU12: Edición rechazada sin canciones")
    void updateAlbum_withNullSongs_throwsAlbumValidationException() {

        AlbumRequest requestConSongsNulo = new AlbumRequest(
                validRequest.title(),
                validRequest.releaseYear(),
                validRequest.cover(),
                validRequest.artistId(),
                validRequest.genreId(),
                null
        );

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(anyString(), anyLong(), anyLong())).thenReturn(false);

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.updateAlbum(album.getId(), requestConSongsNulo, ownerUser.getId().toString()));
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    @Test
    @DisplayName("CP04 - HU12: Edición rechazada por menos de dos canciones")
    void updateAlbum_withLessThanTwoSongs_throwsAlbumValidationException() {

        AlbumRequest requestConUnaCancion = new AlbumRequest(
                validRequest.title(),
                validRequest.releaseYear(),
                validRequest.cover(),
                validRequest.artistId(),
                validRequest.genreId(),
                List.of(validSongRequest)
        );

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(validRequest.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(anyString(), anyLong(), anyLong())).thenReturn(false);

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.updateAlbum(album.getId(), requestConUnaCancion, ownerUser.getId().toString()));
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }


    @Test
    @DisplayName("CP05 - HU12: Edición rechazada por artista no autorizado")
    void updateAlbum_withWrongArtist_throwsAccessDeniedException() {

        Artist otroArtista = new Artist();
        otroArtista.setId(999L);
        album.setArtist(otroArtista);

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genre.getId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(anyString(), anyLong(), anyLong())).thenReturn(false);

        Exception ex = assertThrows(AccessDeniedException.class,
                () -> albumService.updateAlbum(album.getId(), validRequest, ownerUser.getId().toString()));
        assertEquals("No puedes modificar este álbum.", ex.getMessage());
    }

    @Test
    @DisplayName("CP06 - HU12: Edición fallida por ingresar una canción no existente para el artista")
    void updateAlbum_fails_whenSongDoesNotExistForArtist() {

        AlbumRequest request = new AlbumRequest(
                "Álbum Editado", 2023, "cover.jpg", artist.getId(), genre.getId(),
                List.of(new SongRequest("Desconocida", true, artist.getId()), validSongRequest)
        );

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(request.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(request.title(), artist.getId(), album.getId())).thenReturn(false);

        when(songRepository.existsByTitleIgnoreCaseAndArtistId(eq("Desconocida"), eq(artist.getId()))).thenReturn(false);
        when(songRepository.existsByTitleIgnoreCaseAndArtistId(eq(validSongRequest.title()), eq(artist.getId()))).thenReturn(true);

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.updateAlbum(album.getId(), request, ownerUser.getId().toString()));
        assertTrue(ex.getMessage().contains("no existe para este artista"));
    }

    @Test
    @DisplayName("CP07 - HU12: Edición fallida por ingresar una canción no perteneciente al artista")
    void updateAlbum_fails_whenSongNotBelongsToArtist() {

        AlbumRequest request = new AlbumRequest(
                "Álbum Editado", 2023, "cover.jpg", artist.getId(), genre.getId(),
                List.of(validSongRequest, new SongRequest("Ajena", true, 999L))
        );

        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));
        when(genreRepository.findById(request.genreId())).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(request.title(), artist.getId(), album.getId())).thenReturn(false);

        when(songRepository.existsByTitleIgnoreCaseAndArtistId(anyString(), eq(artist.getId()))).thenReturn(true);
        when(songRepository.findAll()).thenReturn(List.of(
                getMockSong(validSongRequest.title(), artist)
        ));

        AlbumValidationException ex = assertThrows(AlbumValidationException.class,
                () -> albumService.updateAlbum(album.getId(), request, ownerUser.getId().toString()));
        assertTrue(ex.getMessage().contains("no pertenece al artista"));
    }




    @Test
    @DisplayName("CP01 - HU27: Eliminación exitosa de un álbum propio")
    void deleteAlbum_asOwner_success() {
        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(artist));

        assertDoesNotThrow(() -> albumService.deleteAlbum(album.getId(), ownerUser.getId().toString()));
        verify(albumRepository).delete(album);
    }


    @Test
    @DisplayName("CP02 - HU27: Eliminacion exitosa de un álbum como ADMIN")
    void deleteAlbum_asAdmin_success() {
        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));

        assertDoesNotThrow(() -> albumService.deleteAlbum(album.getId(), adminUser.getId().toString()));
        verify(albumRepository).delete(album);
    }


    @Test
    @DisplayName("CP03 - HU27: Eliminación fallida por falta de autorización")
    void deleteAlbum_withoutPermission_throwsException() {
        when(albumRepository.findById(album.getId())).thenReturn(Optional.of(album));
        when(userRepository.findById(ownerUser.getId())).thenReturn(Optional.of(ownerUser));
        Artist otroArtista = new Artist();
        otroArtista.setId(99L);
        when(artistRepository.findByUserId(ownerUser.getId())).thenReturn(Optional.of(otroArtista));

        assertThrows(AlbumPermissionException.class,
                () -> albumService.deleteAlbum(album.getId(), ownerUser.getId().toString()));
    }


}
