package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.mapper.AlbumMapper;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.AlbumRepository;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.GenreRepository;
import com.dylabs.zuko.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.util.List;
import java.util.Optional;

public class AlbumServiceUnitTest {

    @Mock
    private AlbumRepository albumRepository;
    @Mock
    private AlbumMapper albumMapper;
    @Mock
    private SongMapper songMapper;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        // Inicialización básica de objetos comunes
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
    }


    // US-10: Crear álbum

    // Escenario 1: Registro exitoso del álbum con todos los datos válidos
    @Test
    @DisplayName("CP01 - HU10 Registro exitoso del álbum con todos los datos válidos")
    void createAlbum_withValidData_returnsAlbumSuccessfully() {
        // Arrange
        String albumTitle = "Mi Primer Álbum";
        int year = 2024;
        String cover = null; // o pon una cadena si quieres testear el cover
        Long genreId = genre.getId();
        Long artistId = artist.getId();

        // Crear SongRequest para el request
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest(song1.getTitle(), song1.isPublicSong(), artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest(song2.getTitle(), song2.isPublicSong(), artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);

        // AlbumRequest real
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        // Album entidad
        Album album = new Album();
        album.setId(10L);
        album.setTitle(albumTitle);
        album.setReleaseYear(year);
        album.setCover(cover);
        album.setGenre(genre);
        album.setArtist(artist);
        album.setSongs(java.util.List.of(song1, song2));

        // AlbumResponse esperado
        java.util.List<com.dylabs.zuko.dto.response.AlbumSongSummaryResponse> songSummaries = java.util.List.of(
                new com.dylabs.zuko.dto.response.AlbumSongSummaryResponse(song1.getTitle()),
                new com.dylabs.zuko.dto.response.AlbumSongSummaryResponse(song2.getTitle())
        );
        com.dylabs.zuko.dto.response.AlbumResponse expectedResponse = new com.dylabs.zuko.dto.response.AlbumResponse(
                album.getId(), albumTitle, year, cover, artist.getName(), genre.getName(), songSummaries
        );

        // Mock: artista existe
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        // Mock: género existe
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));

        // Mock: no existe álbum con mismo título
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(albumTitle, artistId)).thenReturn(false);
        // Mock: mapeo de canciones (SongMapper)
        when(songMapper.toSongEntity(songRequest1, artist)).thenReturn(song1);
        when(songMapper.toSongEntity(songRequest2, artist)).thenReturn(song2);
        // Mock: mapeo de request a entidad
        when(albumMapper.toAlbumEntity(request, artist, genre)).thenReturn(album);
        // Mock: guardar álbum
        when(albumRepository.save(album)).thenReturn(album);
        // Mock: mapeo de entidad a respuesta
        when(albumMapper.toResponse(album)).thenReturn(expectedResponse);

        // Act
        AlbumResponse response = albumService.createAlbum(request);

        // Assert
        assertEquals(albumTitle, response.title());
        assertEquals(year, response.releaseYear());
        assertEquals(cover, response.cover());
        assertEquals(artist.getName(), response.artistName());
        assertEquals(genre.getName(), response.genreName());
        assertEquals(songSummaries, response.songs());
        verify(albumRepository).save(any(Album.class));
    }

    // Escenario 2: Registro fallido por menos de dos canciones
    @Test
    @DisplayName("CP02 - HU10 Registro fallido por menos de dos canciones")
    void createAlbum_withOneSong_throwsAtLeastTwoSongsException() {
        // Arrange
        String albumTitle = "Álbum con una canción";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Solo canción", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1);

        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        // Mock: artista y género existen
        when(artistRepository.findById(artistId)).thenReturn(java.util.Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(java.util.Optional.of(genre));

        // Act & Assert: el service debe lanzar excepción por menos de dos canciones
        assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumValidationException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción de validación porque el álbum tiene menos de dos canciones"
        );
    }

    // Escenario 3: Registro fallido al no tener canciones
    @Test
    @DisplayName("CP02b - HU10 Rechazar álbum con lista de canciones nula")
    void createAlbum_withNullSongs_throwsAtLeastTwoSongsException() {
        // Arrange
        String albumTitle = "Álbum sin canciones";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                albumTitle, year, cover, artistId, genreId, null
        );
        // Mock: artista y género existen
        when(artistRepository.findById(artistId)).thenReturn(java.util.Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(java.util.Optional.of(genre));
        // Act & Assert
        assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumValidationException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción de validación porque la lista de canciones es nula"
        );
    }

    // Escenario 4: Campos obligatorios faltantes
    @Test
    @DisplayName("CP03 - HU10 Rechazar álbum por campos obligatorios faltantes")
    void createAlbum_withMissingTitleOrYear_throwsValidationException() {
        // Arrange
        // TODO: Preparar artista, omitir título o año

        // Arrange
        String albumTitle = null; // Campo obligatorio faltante
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Canción 1", true, artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest("Canción 2", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);

        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        // Mock: artista y género existen
        when(artistRepository.findById(artistId)).thenReturn(java.util.Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(java.util.Optional.of(genre));
        // Mock: el mapper lanza excepción si el título es null
        when(albumMapper.toAlbumEntity(
                argThat(req -> req.title() == null),
                any(),
                any()
        )).thenThrow(new com.dylabs.zuko.exception.albumExceptions.AlbumValidationException("El título es obligatorio"));

        // Act & Assert: el service debe lanzar excepción por campos obligatorios faltantes
        assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumValidationException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción de validación por campos obligatorios faltantes"
        );
    }

    // Escenario 5: Registro fallido por título duplicado
    @Test
    @DisplayName("CP04 - HU10 Registro fallido por título duplicado")
    void createAlbum_withDuplicateTitle_throwsDuplicateTitleException() {
        // Arrange
        String albumTitle = "Álbum Duplicado";
        int year = 2024;
        String cover = null;
        Long genreId = genre.getId();
        Long artistId = artist.getId();
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Canción 1", true, artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest("Canción 2", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);

        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                albumTitle, year, cover, artistId, genreId, songRequests
        );

        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistId(albumTitle, artistId)).thenReturn(true);

        // Act & Assert
        assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException.class,
                () -> albumService.createAlbum(request),
                "Se esperaba excepción por título duplicado de álbum para el artista"
        );
    }



    // US-11: Ver detalle de álbum

    // Escenario 1: Consulta exitosa de álbum
    @Test
    @DisplayName("CP05 - HU11 Obtener detalle de álbum por ID válido")
    void getAlbumById_withValidId_returnsAlbumDetails() {
        // Arrange
        Long albumId = 10L;
        String albumTitle = "Álbum Detalle";
        int year = 2024;
        String cover = "cover.jpg";
        String artistName = artist.getName();
        String genreName = genre.getName();
        List<com.dylabs.zuko.dto.response.AlbumSongSummaryResponse> songSummaries = List.of(
                new com.dylabs.zuko.dto.response.AlbumSongSummaryResponse("Canción 1"),
                new com.dylabs.zuko.dto.response.AlbumSongSummaryResponse("Canción 2")
        );
        Album album = new Album();
        album.setId(albumId);
        album.setTitle(albumTitle);
        album.setReleaseYear(year);
        album.setCover(cover);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setSongs(List.of(song1, song2));
        com.dylabs.zuko.dto.response.AlbumResponse expectedResponse = new com.dylabs.zuko.dto.response.AlbumResponse(
                albumId, albumTitle, year, cover, artistName, genreName, songSummaries
        );
        // Mocks
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(albumMapper.toResponse(album)).thenReturn(expectedResponse);
        // Act
        com.dylabs.zuko.dto.response.AlbumResponse response = albumService.getAlbumById(albumId);
        // Assert
        assertEquals(albumId, response.id());
        assertEquals(albumTitle, response.title());
        assertEquals(year, response.releaseYear());
        assertEquals(cover, response.cover());
        assertEquals(artistName, response.artistName());
        assertEquals(genreName, response.genreName());
        assertEquals(songSummaries, response.songs());
    }

    // Escenario 2: Álbum inexistente
    @Test
    @DisplayName("CP06 - HU11 Lanza excepción si el álbum no existe")
    void getAlbumById_withInvalidId_throwsAlbumNotFoundException() {
        // Arrange
        Long invalidAlbumId = 99L;
        when(albumRepository.findById(invalidAlbumId)).thenReturn(Optional.empty());
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException.class,
                () -> albumService.getAlbumById(invalidAlbumId),
                "Se esperaba excepción por álbum no encontrado"
        );
        assertEquals("Álbum no disponible.", ex.getMessage());
    }



    // US-12: Editar contenido del álbum

    // Escenario 1: Edición exitosa con datos válidos
    @Test
    @DisplayName("CP07 - HU12 Edición exitosa con datos válidos")
    void updateAlbum_withValidData_updatesSuccessfully() {
        // Arrange
        Long albumId = 1L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        String newTitle = "Nuevo Álbum";
        int newYear = 2025;
        String newCover = "nueva_portada.jpg";
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Canción 1", true, artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest("Canción 2", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                newTitle, newYear, newCover, artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        album.setGenre(genre);
        album.setSongs(List.of(song1, song2));
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(newTitle, artistId, albumId)).thenReturn(false);
        // Mock del mapper: no lanza excepción
        doNothing().when(albumMapper).updateAlbumFromRequest(album, request, genre, artist);
        com.dylabs.zuko.dto.response.AlbumResponse expectedResponse = new com.dylabs.zuko.dto.response.AlbumResponse(
                albumId, newTitle, newYear, newCover, artist.getName(), genre.getName(), List.of()
        );
        when(albumMapper.toResponse(album)).thenReturn(expectedResponse);
        // Act
        com.dylabs.zuko.dto.response.AlbumResponse response = albumService.updateAlbum(albumId, request);
        // Assert
        assertEquals(newTitle, response.title());
        assertEquals(newYear, response.releaseYear());
        assertEquals(newCover, response.cover());
        assertEquals(artist.getName(), response.artistName());
        assertEquals(genre.getName(), response.genreName());
        verify(albumRepository).save(album);
    }

    // Escenario 2: Edición rechazada por menos de dos canciones
    @Test
    @DisplayName("CP08 - HU12 Edición rechazada por menos de dos canciones")
    void updateAlbum_withOneSong_throwsValidationException() {
        // Arrange
        Long albumId = 2L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        var songRequest = new com.dylabs.zuko.dto.request.SongRequest("Única canción", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest);
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                "Álbum con una canción", 2024, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        album.setGenre(genre);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumValidationException.class,
                () -> albumService.updateAlbum(albumId, request),
                "Se esperaba excepción por intentar dejar solo una canción"
        );
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    // Escenario 3: Edición rechazada sin canciones
    @Test
    @DisplayName("CP12 - HU12 Edición rechazada sin canciones")
    void updateAlbum_withNullSongList_throwsValidationException() {
        // Arrange
        Long albumId = 6L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                "Álbum sin canciones", 2024, "cover.jpg", artistId, genreId, null
        );
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        album.setGenre(genre);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumValidationException.class,
                () -> albumService.updateAlbum(albumId, request),
                "Se esperaba excepción por lista de canciones null"
        );
        assertEquals("El álbum debe contener al menos dos canciones.", ex.getMessage());
    }

    // Escenario 4: Edición fallida por campos vacíos o inválidos
    @Test
    @DisplayName("CP09 - HU12 Edición fallida por campos vacíos o inválidos")
    void updateAlbum_withMissingRequiredFields_throwsValidationException() {
        // Arrange
        Long albumId = 3L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        // Título nulo (inválido)
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Canción 1", true, artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest("Canción 2", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                null, 2024, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        album.setGenre(genre);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        // Simula que el mapper lanza la excepción de validación
        doThrow(new com.dylabs.zuko.exception.albumExceptions.AlbumValidationException("El título es obligatorio")).when(albumMapper).updateAlbumFromRequest(album, request, genre, artist);
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumValidationException.class,
                () -> albumService.updateAlbum(albumId, request),
                "Se esperaba excepción por campo obligatorio faltante"
        );
        assertEquals("El título es obligatorio", ex.getMessage());
    }

    // Escenario 5: Edición rechazada por artista no autorizado
    @Test
    @DisplayName("CP10 - HU12 Edición rechazada por artista no autorizado")
    void updateAlbum_whenUserIsNotOwner_throwsPermissionException() {
        // Arrange
        Long albumId = 4L;
        Long artistId = 999L; // No es el dueño real
        Long genreId = genre.getId();
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Canción 1", true, artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest("Canción 2", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                "Álbum ajeno", 2024, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist); // El artista real es otro
        album.setGenre(genre);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(new Artist())); // Artista distinto
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumPermissionException.class,
                () -> albumService.updateAlbum(albumId, request),
                "Se esperaba excepción por falta de permisos"
        );
        assertEquals("No tienes permiso para editar este álbum.", ex.getMessage());
    }

    // Escenario 6: Edición rechazada por título ya registrado
    @Test
    @DisplayName("CP11 - HU12 Edición rechazada por título ya registrado")
    void updateAlbum_withDuplicateTitle_throwsDuplicateTitleException() {
        // Arrange
        Long albumId = 5L;
        Long artistId = artist.getId();
        Long genreId = genre.getId();
        String duplicateTitle = "Título Duplicado";
        var songRequest1 = new com.dylabs.zuko.dto.request.SongRequest("Canción 1", true, artistId);
        var songRequest2 = new com.dylabs.zuko.dto.request.SongRequest("Canción 2", true, artistId);
        java.util.List<com.dylabs.zuko.dto.request.SongRequest> songRequests = java.util.List.of(songRequest1, songRequest2);
        com.dylabs.zuko.dto.request.AlbumRequest request = new com.dylabs.zuko.dto.request.AlbumRequest(
                duplicateTitle, 2024, "cover.jpg", artistId, genreId, songRequests
        );
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        album.setGenre(genre);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        when(genreRepository.findById(genreId)).thenReturn(Optional.of(genre));
        when(albumRepository.existsByTitleIgnoreCaseAndArtistIdAndIdNot(duplicateTitle, artistId, albumId)).thenReturn(true);
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException.class,
                () -> albumService.updateAlbum(albumId, request),
                "Se esperaba excepción por título duplicado de álbum para el artista"
        );
        assertEquals("El título del álbum ya existe para este artista.", ex.getMessage());
    }



    // US-27 Eliminar álbum

    // Escenario 1: Eliminación exitosa de un álbum propio
    @Test
    @DisplayName("CP13 - HU27 Eliminación exitosa de un álbum propio")
    void deleteAlbum_withValidOwner_deletesSuccessfully() {
        // Arrange
        Long albumId = 100L;
        Long artistId = artist.getId();
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(artistId)).thenReturn(Optional.of(artist));
        // Act
        albumService.deleteAlbum(albumId, artistId);
        // Assert
        verify(albumRepository).delete(album);
    }

    // Escenario 2: Eliminación rechazada por falta de autorización
    @Test
    @DisplayName("CP14 - HU27 Eliminación rechazada por falta de autorización")
    void deleteAlbum_whenUserIsNotOwner_throwsPermissionException() {
        // Arrange
        Long albumId = 101L;
        Long artistId = 1L; // Dueño real
        Long anotherArtistId = 2L; // No es el dueño
        Artist anotherArtist = new Artist();
        anotherArtist.setId(anotherArtistId);
        Album album = new Album();
        album.setId(albumId);
        album.setArtist(artist);
        when(albumRepository.findById(albumId)).thenReturn(Optional.of(album));
        when(artistRepository.findById(anotherArtistId)).thenReturn(Optional.of(anotherArtist));
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumPermissionException.class,
                () -> albumService.deleteAlbum(albumId, anotherArtistId),
                "Se esperaba excepción por falta de permisos al eliminar álbum ajeno"
        );
        assertEquals("No tienes permiso para eliminar este álbum.", ex.getMessage());
        verify(albumRepository, never()).delete(any());
    }

    // Escenario 3: Eliminación fallida por álbum inexistente
    @Test
    @DisplayName("CP15 - HU27 Eliminación fallida por álbum inexistente")
    void deleteAlbum_withInvalidAlbumId_throwsNotFoundException() {
        // Arrange
        Long albumId = 999L;
        Long artistId = artist.getId();
        when(albumRepository.findById(albumId)).thenReturn(Optional.empty());
        // Act & Assert
        Exception ex = assertThrows(
                com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException.class,
                () -> albumService.deleteAlbum(albumId, artistId),
                "Se esperaba excepción por álbum no encontrado al eliminar"
        );
        assertEquals("Álbum no disponible.", ex.getMessage());
        verify(albumRepository, never()).delete(any());
    }

}