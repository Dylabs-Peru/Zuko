package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.songExceptions.SongAlreadyExistException;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.SongRepository;
import com.dylabs.zuko.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.access.AccessDeniedException;


import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class SongServiceUnitTest {

    @Mock
    private SongRepository repository;

    @Mock
    private SongMapper mapper;

    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private SongService songService;

    private Artist artist;
    private User adminUser;
    private User artistUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        artist = new Artist();
        artist.setId(1L);
        artist.setName("Bruno Mars");

        adminUser = new User();
        adminUser.setId(10L);
        adminUser.setUserRoleName("ADMIN");

        artistUser = new User();
        artistUser.setId(20L);
        artistUser.setUserRoleName("ARTIST");
    }

    // Crear canción con rol ADMIN
    @Test
    @DisplayName("CP01 - HU01 - Crear canción como Administrador exitosamente")
    void createSongAsAdminSuccess() {
        SongRequest request = new SongRequest("Just The Way You Are", true, artist.getId());
        Song song = new Song(request.title(), request.isPublicSong());
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        SongResponse expectedResponse = new SongResponse(1L, request.title(), request.isPublicSong(), song.getReleaseDate(), "Canción registrada exitosamente", artist.getId(), artist.getName());

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(artistRepository.findById(artist.getId())).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())).thenReturn(false);
        when(mapper.toSongEntity(request, artist)).thenReturn(song);
        when(repository.save(song)).thenReturn(song);
        when(mapper.toResponse(song)).thenReturn(expectedResponse);

        SongResponse response = songService.createSong(request, String.valueOf(adminUser.getId()));

        assertEquals("Canción registrada exitosamente", response.message());
        assertEquals(request.title(), response.title());
        verify(repository).save(any(Song.class));
    }

    // Crear canción con rol ARTIST
    @Test
    @DisplayName("CP02 - HU01 - Crear canción como Artista exitosamente")
    void createSongAsArtistSuccess() {
        SongRequest request = new SongRequest("Locked Out of Heaven", true, 99L);
        Song song = new Song(request.title(), request.isPublicSong());
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        SongResponse expectedResponse = new SongResponse(1L, request.title(), request.isPublicSong(), song.getReleaseDate(), "Canción registrada exitosamente", artist.getId(), artist.getName());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())).thenReturn(false);
        when(mapper.toSongEntity(request, artist)).thenReturn(song);
        when(repository.save(song)).thenReturn(song);
        when(mapper.toResponse(song)).thenReturn(expectedResponse);

        SongResponse response = songService.createSong(request, String.valueOf(artistUser.getId()));

        assertEquals("Canción registrada exitosamente", response.message());
        assertEquals(request.title(), response.title());
        verify(repository).save(any(Song.class));
    }

    @Test
    @DisplayName("CP03 - HU01  Crear canción con título duplicado")
    void createSongTitleDuplicateThrows() {
        SongRequest request = new SongRequest("Grenade", true, artist.getId());

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(artistRepository.findById(artist.getId())).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), artist.getId())).thenReturn(true);

        assertThrows(SongAlreadyExistException.class, () -> songService.createSong(request, String.valueOf(adminUser.getId())));
        verify(repository, never()).save(any());
    }

    // Editar canción como ADMIN exitosamente
    @Test
    @DisplayName("CP01 - HU02 - Editar canción como Administrador exitosamente")
    void updateSongAsAdminSuccess() {
        Long songId = 1L;
        SongRequest request = new SongRequest("Grenade", true, artist.getId());

        Song song = new Song("Old Title", false);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findById(request.artistId())).thenReturn(Optional.of(artist));
        when(repository.save(song)).thenReturn(song);

        SongResponse response = songService.updateSong(songId, request, String.valueOf(adminUser.getId()));

        assertEquals(request.title(), response.title());
        assertEquals("La canción ha sido actualizada correctamente.", response.message());
    }

    // Editar canción como ARTIST exitosamente
    @Test
    @DisplayName("CP02 - HU02 - Editar canción como Artista exitosamente")
    void updateSongAsArtistSuccess() {
        Long songId = 1L;
        SongRequest request = new SongRequest("Locked Out of Heaven", true, 99L);

        Song song = new Song("Grenade", false);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.of(artist));
        when(repository.save(song)).thenReturn(song);

        SongResponse response = songService.updateSong(songId, request, String.valueOf(artistUser.getId()));

        assertEquals(request.title(), response.title());
        assertEquals("La canción ha sido actualizada correctamente.", response.message());
    }

    @Test
    @DisplayName("CP03 - HU02 - Editar canción inexistente")
    void updateSongNotFoundThrows() {
        Long songId = 99L;
        SongRequest request = new SongRequest("Locked Out of Heaven", true, artist.getId());

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(repository.findById(songId)).thenReturn(Optional.empty());

        SongNotFoundException exception = assertThrows(SongNotFoundException.class,
                () -> songService.updateSong(songId, request, String.valueOf(adminUser.getId())));

        assertEquals("Canción no encontrada", exception.getMessage());
        verify(repository, never()).save(any());
    }

    // Eliminar canción como ADMIN exitosamente
    @Test
    @DisplayName("CP01 - HU03 - Eliminar canción como Administrador exitosamente")
    void deleteSongAsAdminSuccess() {
        Long songId = 1L;
        Song song = new Song("Treasure", true);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));

        SongResponse response = songService.deleteSong(songId, String.valueOf(adminUser.getId()));

        assertEquals("La canción ha sido eliminada correctamente.", response.message());
        verify(repository).delete(song);
    }

    // Eliminar canción como ARTIST exitosamente
    @Test
    @DisplayName("CP02 - HU03 - Eliminar canción como Artista exitosamente")
    void deleteSongAsArtistSuccess() {
        Long songId = 1L;
        Song song = new Song("Treasure", true);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.of(artist));

        SongResponse response = songService.deleteSong(songId, String.valueOf(artistUser.getId()));

        assertEquals("La canción ha sido eliminada correctamente.", response.message());
        verify(repository).delete(song);
    }

    @Test
    @DisplayName("CP03 - HU03 - Eliminar canción inexistente")
    void deleteSongNotFoundThrows() {
        Long songId = 999L;

        when(userRepository.findById(adminUser.getId())).thenReturn(Optional.of(adminUser));
        when(repository.findById(songId)).thenReturn(Optional.empty());

        SongNotFoundException exception = assertThrows(SongNotFoundException.class,
                () -> songService.deleteSong(songId, String.valueOf(adminUser.getId())));

        assertEquals("La canción no se encontró.", exception.getMessage());
    }


    @Test
    @DisplayName("CP04 - HU02 - Editar canción como Usuario sin perfil de artista")
    void updateSongAsArtistWithoutProfileThrows() {
        Long songId = 1L;
        SongRequest request = new SongRequest("New Title", true, 99L);
        Song song = new Song("Old Title", false);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.empty());

        ArtistNotFoundException exception = assertThrows(ArtistNotFoundException.class,
                () -> songService.updateSong(songId, request, String.valueOf(artistUser.getId())));

        assertEquals("No tienes un perfil de artista.", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CP05 - HU02 - Editar canción como Artista pero no le pertenece la canción")
    void updateSongAsArtistNotOwnerThrows() {
        Long songId = 1L;
        SongRequest request = new SongRequest("New Title", true, 99L);
        Song song = new Song("Old Title", false);
        song.setId(songId);
        Artist anotherArtist = new Artist();
        anotherArtist.setId(999L);
        song.setArtist(anotherArtist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.of(artist));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> songService.updateSong(songId, request, String.valueOf(artistUser.getId())));

        assertEquals("No puedes modificar esta canción.", exception.getMessage());
        verify(repository, never()).save(any());
    }


    @Test
    @DisplayName("CP04 - HU03 -Eliminar canción como Usuario sin perfil de artista")
    void deleteSongAsArtistWithoutProfileThrows() {
        Long songId = 1L;
        Song song = new Song("Title", true);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.empty());

        ArtistNotFoundException exception = assertThrows(ArtistNotFoundException.class,
                () -> songService.deleteSong(songId, String.valueOf(artistUser.getId())));

        assertEquals("No tienes un perfil de artista.", exception.getMessage());
        verify(repository, never()).delete(any());
    }


    @Test
    @DisplayName("CP05 - HU03 - Eliminar canción como Artista pero no le pertenece la canción")
    void deleteSongAsArtistNotOwnerThrows() {
        Long songId = 1L;
        Song song = new Song("Title", true);
        song.setId(songId);
        Artist anotherArtist = new Artist();
        anotherArtist.setId(999L);
        song.setArtist(anotherArtist);
        song.setReleaseDate(LocalDate.now());

        when(userRepository.findById(artistUser.getId())).thenReturn(Optional.of(artistUser));
        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findByUserId(artistUser.getId())).thenReturn(Optional.of(artist));

        AccessDeniedException exception = assertThrows(AccessDeniedException.class,
                () -> songService.deleteSong(songId, String.valueOf(artistUser.getId())));

        assertEquals("No puedes eliminar esta canción.", exception.getMessage());
        verify(repository, never()).delete(any());
    }
}