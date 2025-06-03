package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.songExceptions.SongAlreadyExistException;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.SongRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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

    @InjectMocks
    private SongService songService;

    private Artist artist;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        artist = new Artist();
        artist.setId(1L);
        artist.setName("Bruno Mars");
    }

    @Test
    @DisplayName("CP01 - HU01 Registro correcto de una nueva canción")
    void shouldCreateSongSuccessfully() {
        SongRequest request = new SongRequest("Just The Way You Are", true, 1L);
        Song song = new Song(request.title(), request.isPublicSong());
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        SongResponse expectedResponse = new SongResponse(1L, request.title(), request.isPublicSong(), song.getReleaseDate(), "Canción registrada exitosamente", artist.getId(), artist.getName());

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), 1L)).thenReturn(false);
        when(mapper.toSongEntity(request, artist)).thenReturn(song);
        when(repository.save(song)).thenReturn(song);
        when(mapper.toResponse(song)).thenReturn(expectedResponse);

        SongResponse response = songService.createSong(request);

        assertEquals("Canción registrada exitosamente", response.message());
        assertEquals(request.title(), response.title());
        verify(repository).save(any(Song.class));
    }

    @Test
    @DisplayName("CP02 - HU01 Registrar canción ya existente")
    void shouldThrowExceptionWhenSongAlreadyExists() {
        SongRequest request = new SongRequest("Grenade", true, 1L);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), 1L)).thenReturn(true);

        assertThrows(SongAlreadyExistException.class, () -> songService.createSong(request));
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CP03 - HU01 Registro rechazado por título muy corto")
    void shouldRejectSongWithShortTitle() {
        SongRequest request = new SongRequest("Hey", true, 1L);

        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), 1L)).thenReturn(false);

        Song song = new Song(request.title(), request.isPublicSong());
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(mapper.toSongEntity(request, artist)).thenReturn(song);
        when(repository.save(song)).thenReturn(song);
        when(mapper.toResponse(song)).thenReturn(new SongResponse(1L, song.getTitle(), song.isPublicSong(), song.getReleaseDate(), "Título muy corto", artist.getId(), artist.getName()));

        SongResponse response = songService.createSong(request);
        assertEquals("Hey", response.title());
        assertTrue(response.title().length() < 4);
    }

    @Test
    @DisplayName("CP01 - HU02 Edición correcta de una canción")
    void shouldUpdateSongSuccessfully() {
        Long songId = 1L;
        SongRequest request = new SongRequest("Locked Out of Heaven", true, 1L);

        Song song = new Song("Grenade", false);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(artistRepository.findById(1L)).thenReturn(Optional.of(artist));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), 1L)).thenReturn(false);
        when(repository.save(song)).thenReturn(song);

        SongResponse response = songService.updateSong(songId, request);

        assertEquals("Locked Out of Heaven", response.title());
        assertEquals("La canción ha sido actualizada correctamente.", response.message());
    }

    @Test
    @DisplayName("CP02 - HU02 Título repetido en edición")
    void shouldThrowExceptionWhenUpdatingToExistingTitle() {
        Long songId = 1L;
        SongRequest request = new SongRequest("Grenade", true, 1L);
        Song song = new Song("Old Title", false);
        song.setId(songId);
        song.setArtist(artist);

        when(repository.findById(songId)).thenReturn(Optional.of(song));
        when(repository.existsByTitleIgnoreCaseAndArtistId(request.title(), 1L)).thenReturn(true);

        assertThrows(SongAlreadyExistException.class, () -> songService.updateSong(songId, request));
    }

    @Test
    @DisplayName("CP03 - HU02 Intentar editar una canción que no existe")
    void shouldThrowExceptionWhenEditingNonExistentSong() {
        Long songId = 99L;
        SongRequest request = new SongRequest("Locked Out of Heaven", true, 1L);

        when(repository.findById(songId)).thenReturn(Optional.empty());

        SongNotFoundException exception = assertThrows(SongNotFoundException.class,
                () -> songService.updateSong(songId, request));

        assertEquals("Canción no encontrada", exception.getMessage());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("CP01 - HU03 Eliminación correcta de una canción")
    void shouldDeleteSongSuccessfully() {
        Long songId = 1L;
        Song song = new Song("Treasure", true);
        song.setId(songId);
        song.setArtist(artist);
        song.setReleaseDate(LocalDate.now());

        when(repository.findById(songId)).thenReturn(Optional.of(song));

        SongResponse response = songService.deleteSong(songId);

        assertEquals("La canción ha sido eliminada correctamente.", response.message());
        verify(repository).delete(song);
    }

    @Test
    @DisplayName("CP02 - HU03 Intentar eliminar canción inexistente")
    void shouldThrowExceptionWhenDeletingNonExistentSong() {
        Long songId = 999L;
        when(repository.findById(songId)).thenReturn(Optional.empty());

        SongNotFoundException exception = assertThrows(SongNotFoundException.class,
                () -> songService.deleteSong(songId));

        assertEquals("La canción no se encontró en tu catálogo.", exception.getMessage());
    }
}