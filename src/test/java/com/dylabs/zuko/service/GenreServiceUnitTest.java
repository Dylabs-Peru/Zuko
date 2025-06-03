package com.dylabs.zuko.service;


import com.dylabs.zuko.dto.request.GenreRequest;
import com.dylabs.zuko.dto.response.GenreResponse;
import com.dylabs.zuko.exception.genreExeptions.GenreAlreadyExistsException;
import com.dylabs.zuko.exception.genreExeptions.GenreInUseException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.mapper.GenreMapper;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.repository.AlbumRepository;
import com.dylabs.zuko.repository.GenreRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;



class GenreServiceUnitTest {
    @Mock
    private GenreRepository genreRepository;
    @Mock
    private GenreMapper genreMapper;
    @InjectMocks
    private GenreService genreService;

    @Mock
    private AlbumRepository albumRepository;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    @DisplayName("CP01-HU13 - Listar géneros existentes retorna lista y 200 OK")
    void getGenres_whenGenresExist_returnsGenreList(){
        List<Genre> genreList = List.of(new Genre(1L, "Rock", "desc"), new Genre(2L,"Pop", "desc2"));
        when(genreRepository.findAll()).thenReturn(genreList);
        when(genreMapper.toResponse(any())).thenAnswer(invocation -> {
            Genre g = invocation.getArgument(0);
            return new GenreResponse(g.getId(), g.getName(), g.getDescription());
        });

        List<GenreResponse> result = genreService.getGenres();

        assertEquals(2, result.size());
        assertEquals("Rock", result.get(0).name());
        assertEquals("Pop", result.get(1).name());
        verify(genreRepository).findAll();
    }

    @Test
    @DisplayName("CP02-HU13 - Listar géneros cuando no existen retorna 404 y mensaje especial")
    void getGenres_whenNoGenres_throwsGenreNotFoundException() {
        when(genreRepository.findAll()).thenReturn(Collections.emptyList());

        GenreNotFoundException ex = assertThrows(GenreNotFoundException.class,
                () -> genreService.getGenres());
        assertEquals("Aún no se han registrado géneros musicales.", ex.getMessage());
    }

    @Test
    @DisplayName("CP01-HU15 - Crear género válido retorna 201 Created")
    void createGenre_withValidData_returnsCreatedGenre() {
        GenreRequest request = new GenreRequest("Jazz", "desc jazz");
        when(genreRepository.existsByNameIgnoreCase("Jazz")).thenReturn(false);
        Genre genre = new Genre(10L, "Jazz", "desc jazz");
        when(genreMapper.toGenreEntity(request)).thenReturn(genre);
        when(genreRepository.save(genre)).thenReturn(genre);
        GenreResponse expected = new GenreResponse(10L, "Jazz", "desc jazz");
        when(genreMapper.toResponse(genre)).thenReturn(expected);

        GenreResponse result = genreService.createGenre(request);

        assertEquals("Jazz", result.name());
        assertEquals("desc jazz", result.description());
        verify(genreRepository).save(genre);
    }

    @Test
    @DisplayName("CP02-HU15 - Crear género con nombre duplicado lanza excepción 409")
    void createGenre_withDuplicateName_throwsGenreAlreadyExistsException() {
        GenreRequest request = new GenreRequest("Rock", "desc rock");
        when(genreRepository.existsByNameIgnoreCase("Rock")).thenReturn(true);

        GenreAlreadyExistsException ex = assertThrows(GenreAlreadyExistsException.class,
                () -> genreService.createGenre(request));
        assertEquals("El genero Rock ya está registrado", ex.getMessage());
    }

    @Test
    @DisplayName("CP01-HU14 - Actualizar género válido retorna género actualizado y 200 OK")
    void updateGenre_withValidData_returnsUpdatedGenre() {
        long id = 2L;
        Genre existing = new Genre(id, "Rock", "desc vieja");
        GenreRequest request = new GenreRequest("Blues", "desc nueva");
        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.existsByNameIgnoreCase("Blues")).thenReturn(false);
        existing.setName("Blues");
        existing.setDescription("desc nueva");
        when(genreRepository.save(existing)).thenReturn(existing);
        GenreResponse response = new GenreResponse(id, "Blues", "desc nueva");
        when(genreMapper.toResponse(existing)).thenReturn(response);

        GenreResponse result = genreService.updateGenre(id, request);

        assertEquals("Blues", result.name());
        assertEquals("desc nueva", result.description());
    }

    @Test
    @DisplayName("CP02-HU14 - Actualizar género inexistente lanza excepción 404")
    void updateGenre_withNonexistentId_throwsGenreNotFoundException() {
        long id = 99L;
        GenreRequest request = new GenreRequest("Jazz", "desc");
        when(genreRepository.findById(id)).thenReturn(Optional.empty());

        GenreNotFoundException ex = assertThrows(GenreNotFoundException.class,
                () -> genreService.updateGenre(id, request));
        assertEquals("El género con id 99 no existe", ex.getMessage());
    }

    @Test
    @DisplayName("CP03-HU14 - Actualizar género con nombre duplicado lanza excepción 409")
    void updateGenre_withDuplicateName_throwsGenreAlreadyExistsException() {
        long id = 2L;
        Genre existing = new Genre(id, "Pop", "desc vieja");
        GenreRequest request = new GenreRequest("Rock", "desc");
        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.existsByNameIgnoreCase("Rock")).thenReturn(true);

        GenreAlreadyExistsException ex = assertThrows(GenreAlreadyExistsException.class,
                () -> genreService.updateGenre(id, request));
        assertEquals("El genero Rock ya está registrado", ex.getMessage());
    }

    @Test
    @DisplayName("CP01-HU28 - Eliminar género existente no asociado a un álbum y retorna 200 OK")
    void deleteGenre_existing_deletesSuccessfully() {
        long id = 10L;
        when(genreRepository.existsById(id)).thenReturn(true);
        when(albumRepository.existsByGenreId(id)).thenReturn(false);

        genreService.deleteGenre(id);

        verify(genreRepository).deleteById(id);
    }

    @Test
    @DisplayName("CP02-HU28 - Eliminar género inexistente lanza excepción 404")
    void deleteGenre_nonExistent_throwsGenreNotFoundException() {
        long id = 99L;
        when(genreRepository.existsById(id)).thenReturn(false);

        GenreNotFoundException ex = assertThrows(GenreNotFoundException.class,
                () -> genreService.deleteGenre(id));
        assertEquals("El género con id 99 no existe", ex.getMessage());
    }

    @Test
    @DisplayName("CP03-HU28 - Eliminar género existente asociado a un álbum lanza excepción 409")
    void deleteGenre_genreInUse_throwsGenreInUseException() {
        long id = 1L;
        when(genreRepository.existsById(id)).thenReturn(true);
        when(albumRepository.existsByGenreId(id)).thenReturn(true);

        GenreInUseException ex = assertThrows(GenreInUseException.class,
                () -> genreService.deleteGenre(id));
        assertEquals("No se puede eliminar el género porque está asociado a uno o más álbumes.", ex.getMessage());
    }

    @Test
    @DisplayName("CP04-HU14 - Actualizar género con el mismo nombre (case-insensitive)  permite actualizar descripción correctamente")
    void updateGenre_withSameName_doesNotThrowException() {
        long id = 5L;
        Genre existing = new Genre(id, "Jazz", "old desc");
        GenreRequest request = new GenreRequest("jazz", "new desc"); // igual, solo distinto casing

        when(genreRepository.findById(id)).thenReturn(Optional.of(existing));
        when(genreRepository.existsByNameIgnoreCase("jazz")).thenReturn(true);
        existing.setName("jazz");
        existing.setDescription("new desc");
        when(genreRepository.save(existing)).thenReturn(existing);
        GenreResponse response = new GenreResponse(id, "jazz", "new desc");
        when(genreMapper.toResponse(existing)).thenReturn(response);

        GenreResponse result = genreService.updateGenre(id, request);

        assertEquals("jazz", result.name());
        assertEquals("new desc", result.description());
    }




}
