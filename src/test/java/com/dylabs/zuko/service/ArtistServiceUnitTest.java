package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.request.UpdateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.artistExeptions.ArtistAlreadyExistsException;
import com.dylabs.zuko.exception.artistExeptions.ArtistValidationException;
import com.dylabs.zuko.mapper.ArtistMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.UserRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

class ArtistServiceUnitTest {
    @Mock
    private ArtistRepository artistRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private ArtistMapper artistMapper;

    @InjectMocks
    private ArtistService artistService;

    private User testUser;
    private Artist testArtist;
    private CreateArtistRequest createRequest;
    private UpdateArtistRequest updateRequest;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testUser");

        testArtist = new Artist();
        testArtist.setId(1L);
        testArtist.setName("Test Artist");
        testArtist.setCountry("Test Country");
        testArtist.setBiography("Test Biography");
        testArtist.setUser(testUser);
        testArtist.setIsActive(true);

        createRequest = new CreateArtistRequest(
                "Test Artist",
                "Test Country",
                "Test Biography"
        );

        updateRequest = new UpdateArtistRequest(
                "Updated Artist",
                "Updated Country",
                "Updated Biography"
        );
    }

    // Crear artista
    @Test
    @DisplayName("CP01-HU07 - Crear artista exitosamente")
    void testCreateArtistSuccessfully() {
        // Arrange
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByName(anyString()))
                .thenReturn(Optional.empty());
        when(artistRepository.findByUserId(anyLong()))
                .thenReturn(Optional.empty());
        when(artistMapper.toEntity(any(), any()))
                .thenReturn(testArtist);
        when(artistRepository.save(any()))
                .thenReturn(testArtist);
        when(artistMapper.toResponse(any()))
                .thenReturn(new ArtistResponse(
                        1L,
                        "Test Artist",
                        "Test Country",
                        "Test Biography",
                        1L,
                        true
                ));


        ArtistResponse response = artistService.createArtist(createRequest, "testUser");


        assertNotNull(response);
        verify(artistRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("CP02-HU07 - Usuario ya tiene artista")
    void testCreateArtistWhenUserAlreadyHasArtist() {

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByUserId(anyLong()))
                .thenReturn(Optional.of(testArtist));


        assertThrows(ArtistAlreadyExistsException.class, () ->
                artistService.createArtist(createRequest, "testUser"));
    }

    @Test
    @DisplayName("CP03-HU07 - Nombre de artista duplicado")
    void testCreateArtistWhenNameAlreadyExists() {

        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByName(anyString()))
                .thenReturn(Optional.of(testArtist));


        assertThrows(ArtistAlreadyExistsException.class, () ->
                artistService.createArtist(createRequest, "testUser"));
    }

    @Test
    @DisplayName("CP04-HU07 - País vacío")
    void testCreateArtistWhenCountryIsEmpty() {

        CreateArtistRequest request = new CreateArtistRequest(
                "Test Artist",
                "",
                "Test Biography"
        );
        when(userRepository.findByUsername(anyString()))
                .thenReturn(Optional.of(testUser));
        when(artistRepository.findByName(anyString()))
                .thenReturn(Optional.empty());
        when(artistRepository.findByUserId(anyLong()))
                .thenReturn(Optional.empty());
        when(artistMapper.toEntity(any(), any()))
                .thenReturn(testArtist);
        when(artistRepository.save(any()))
                .thenReturn(testArtist);
        when(artistMapper.toResponse(any()))
                .thenReturn(new ArtistResponse(
                        1L, "Test Artist", "", "Test Biography", 1L, true
                ));


        ArtistResponse response = artistService.createArtist(request, "testUser");


        assertNotNull(response);
        assertEquals("", response.country());
        verify(artistRepository, times(1)).save(any());
    }

    // Editar artista
    @Test
    @DisplayName("CP01-HU08 - Edición de artista exitosa")
    void testUpdateArtistSuccessfully() {
        // Arrange
        when(artistRepository.findById(1L))
                .thenReturn(Optional.of(testArtist));
        when(artistRepository.findByName("Updated Artist"))
                .thenReturn(Optional.empty());
        when(artistRepository.save(any()))
                .thenReturn(testArtist);
        when(artistMapper.toResponse(testArtist))
                .thenReturn(new ArtistResponse(
                        1L,
                        "Updated Artist",
                        "Updated Country",
                        "Updated Biography",
                        1L,
                        true
                ));


        ArtistResponse response = artistService.updateArtist(1L, updateRequest);


        assertNotNull(response);
        assertEquals("Updated Artist", response.name());
        assertEquals("Updated Country", response.country());
        assertEquals("Updated Biography", response.biography());
        assertTrue(response.isActive());
        verify(artistRepository).findById(1L);
        verify(artistRepository).findByName("Updated Artist");
        verify(artistRepository).save(any());
        verifyNoMoreInteractions(artistRepository);
    }

    @Test
    @DisplayName("CP02-HU08 - Cambio de nombre en uso")
    void testUpdateArtistWhenNameAlreadyExists() {

        when(artistRepository.findById(anyLong()))
                .thenReturn(Optional.of(testArtist));
        when(artistRepository.findByName(anyString()))
                .thenReturn(Optional.of(testArtist));


        assertThrows(ArtistAlreadyExistsException.class, () ->
                artistService.updateArtist(1L, updateRequest));
    }

    @Test
    @DisplayName("CP03-HU08 - Artista no encontrado para editar")
    void testUpdateArtistWhenArtistNotFound() {

        when(artistRepository.findById(anyLong()))
                .thenReturn(Optional.empty());


        assertThrows(ArtistNotFoundException.class, () ->
                artistService.updateArtist(1L, updateRequest));
    }

    @Test
    @DisplayName("CP04-HU08 - Actualizar artista sin modificar el nombre")
    void testUpdateArtistWithNullName() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any())).thenReturn(testArtist);
        when(artistMapper.toResponse(any())).thenReturn(new ArtistResponse(
                1L, "Test Artist", "Test Country", "Test Biography", 1L, true
        ));

        UpdateArtistRequest req = new UpdateArtistRequest(
                null,
                "Test Country",
                "Test Biography"
        );

        ArtistResponse response = artistService.updateArtist(1L, req);

        assertNotNull(response);
        assertEquals("Test Artist", response.name());
    }

    @Test
    @DisplayName("CP05-HU08 - Actualizar artista sin modificar el pais")
    void testUpdateArtistWithNullCountry() {
        testArtist.setCountry("Test Country");
        testArtist.setName("Test Artist");
        testArtist.setBiography("Test Biography");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any())).thenReturn(testArtist);
        when(artistMapper.toResponse(any())).thenAnswer(invocation -> {
            Artist a = invocation.getArgument(0);
            return new ArtistResponse(
                    a.getId(),
                    a.getName(),
                    a.getCountry(),
                    a.getBiography(),
                    a.getUser().getId(),
                    a.getIsActive()
            );
        });

        UpdateArtistRequest req = new UpdateArtistRequest(
                "Test Artist",
                null, // país null
                "Test Biography"
        );

        ArtistResponse response = artistService.updateArtist(1L, req);

        assertNotNull(response);
        assertEquals("Test Country", response.country());
    }

    @Test
    @DisplayName("CP05-HU08- No permite país vacío al editar artista")
    void testUpdateArtistWithEmptyCountryThrowsException() {
        testArtist.setUser(new User());
        testArtist.getUser().setId(1L);
        testArtist.getUser().setUserRoleName("USER");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));

        UpdateArtistRequest req = new UpdateArtistRequest(
                "Test Artist",
                "",
                "Test Biography"
        );

        assertThrows(ArtistValidationException.class, () ->
                artistService.updateArtist(1L, req, "1")
        );
    }

    @Test
    @DisplayName("CP06-HU08 - Actualizar artista sin modificar la biografia")
    void testUpdateArtistWithNullBiography() {
        testArtist.setCountry("Test Country");
        testArtist.setName("Test Artist");
        testArtist.setBiography("Test Biography");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any())).thenReturn(testArtist);
        when(artistMapper.toResponse(any())).thenAnswer(invocation -> {
            Artist a = invocation.getArgument(0);
            return new ArtistResponse(
                    a.getId(),
                    a.getName(),
                    a.getCountry(),
                    a.getBiography(),
                    a.getUser().getId(),
                    a.getIsActive()
            );
        });

        UpdateArtistRequest req = new UpdateArtistRequest(
                "Test Artist",
                "Test Country",
                null
        );

        ArtistResponse response = artistService.updateArtist(1L, req);

        assertNotNull(response);
        assertEquals("Test Biography", response.biography()); // No cambia
    }

    //Security
    @Test
    @DisplayName("CP07-HU08- Solo el dueño puede editar su artista")
    void testUpdateArtistOnlyOwnerCanEdit() {

        testArtist.setUser(new User());
        testArtist.getUser().setId(1L);
        testArtist.getUser().setUserRoleName("USER");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any())).thenReturn(testArtist);
        when(artistMapper.toResponse(any())).thenReturn(new ArtistResponse(
                1L, "Nuevo Nombre", "Test Country", "Test Biography", 1L, true
        ));


        UpdateArtistRequest req = new UpdateArtistRequest(
                "Nuevo Nombre", "Test Country", "Test Biography"
        );

        ArtistResponse response = artistService.updateArtist(1L, req, "1");

        assertNotNull(response);
        assertEquals("Nuevo Nombre", response.name());
    }

    @Test
    @DisplayName("CP08-HU08- No permite editar si no es el dueño")
    void testUpdateArtistNotOwnerThrowsException() {

        testArtist.setUser(new User());
        testArtist.getUser().setId(99L);
        testArtist.getUser().setUserRoleName("USER");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        User otroUsuario = new User();
        otroUsuario.setId(2L);
        otroUsuario.setUserRoleName("USER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(otroUsuario));

        UpdateArtistRequest req = new UpdateArtistRequest(
                "Nuevo Nombre", "Test Country", "Test Biography"
        );

        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                artistService.updateArtist(1L, req, "2")
        );
    }

    @Test
    @DisplayName("CP09-HU08- Admin puede editar cualquier artista")
    void testUpdateArtistAdminCanEditAnyArtist() {

        testArtist.setUser(new User());
        testArtist.getUser().setId(99L);
        testArtist.getUser().setUserRoleName("USER");
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));
        User adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUserRoleName("ADMIN");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(artistRepository.save(any())).thenReturn(testArtist);
        when(artistMapper.toResponse(any())).thenReturn(new ArtistResponse(
                1L, "Nombre Editado Por Admin", "País", "Bio", 99L, true
        ));

        UpdateArtistRequest req = new UpdateArtistRequest(
                "Nombre Editado Por Admin", "País", "Bio"
        );

        ArtistResponse response = artistService.updateArtist(1L, req, "1");

        assertNotNull(response);
        assertEquals("Nombre Editado Por Admin", response.name());
    }

    // Mostrar artista
    @Test
    @DisplayName("CP01-HU09- Listar todos los artistas")
    void testGetAllArtists() {

        when(artistRepository.findAll())
                .thenReturn(List.of(testArtist));
        when(artistMapper.toResponseList(any()))
                .thenReturn(List.of(new ArtistResponse(
                        1L,
                        "Test Artist",
                        "Test Country",
                        "Test Biography",
                        1L,
                        true
                )));


        List<ArtistResponse> response = artistService.getAllArtists();


        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertEquals(1, response.size());
        assertEquals("Test Artist", response.getFirst().name());
        assertEquals("Test Country", response.getFirst().country());
        verify(artistRepository, times(1)).findAll();
        verify(artistMapper, times(1)).toResponseList(any());
    }

    @Test
    @DisplayName("CP02-HU09- Buscar artista por ID existente")
    void testGetArtistById() {

        when(artistRepository.findById(anyLong()))
                .thenReturn(Optional.of(testArtist));
        when(artistMapper.toResponse(any()))
                .thenReturn(new ArtistResponse(
                        1L,
                        "Test Artist",
                        "Test Country",
                        "Test Biography",
                        1L,
                        true
                ));


        ArtistResponse response = artistService.getArtistById(1L);

        assertNotNull(response);
        verify(artistRepository, times(1)).findById(anyLong());
        verify(artistMapper, times(1)).toResponse(any());
    }

    @Test
    @DisplayName("CP03-HU09 - Buscar artista no existente")
    void testSearchArtistsWhenNotFound() {

        when(artistRepository.findByNameContainingIgnoreCase(anyString()))
                .thenReturn(List.of());
        when(artistMapper.toResponseList(anyList()))
                .thenReturn(List.of());


        List<ArtistResponse> response = artistService.searchArtistsByName("nonexistent");


        assertNotNull(response);
        assertTrue(response.isEmpty());
    }

    // Cambiar estado del artista
    //security
    @Test
    @DisplayName("CP03-HU26 - No permite cambiar estado si no es dueño ni admin")
    void testToggleArtistStatusNotOwnerOrAdminThrowsException() {
        when(artistRepository.findById(1L)).thenReturn(Optional.of(testArtist));

        User otroUsuario = new User();
        otroUsuario.setId(2L);
        otroUsuario.setUsername("otroUsuario");
        otroUsuario.setUserRoleName("USER");
        when(userRepository.findById(2L)).thenReturn(Optional.of(otroUsuario));


        assertThrows(org.springframework.security.access.AccessDeniedException.class, () ->
                artistService.toggleArtistActiveStatus(1L, "2")
        );
    }

    @Test
    @DisplayName("CP01-HU26 - Cambiar estado de actividad exitosamente")
    void testToggleArtistStatusSuccessfully() {

        when(artistRepository.findById(1L))
                .thenReturn(Optional.of(testArtist));
        when(artistRepository.save(any()))
                .thenReturn(testArtist);


        artistService.toggleArtistActiveStatus(1L, "1");

        verify(artistRepository).findById(1L);
        verify(artistRepository).save(any());
        verifyNoMoreInteractions(artistRepository);
        verify(artistRepository, times(1)).save(any());
    }

    @Test
    @DisplayName("CP02-HU26 - Artista no encontrado para cambiar estado")
    void testToggleArtistStatusWhenArtistNotFound() {

        when(artistRepository.findById(1L))
                .thenReturn(Optional.empty());


        Exception exception = assertThrows(ArtistNotFoundException.class, () ->
                artistService.toggleArtistActiveStatus(1L, "testUser"));
        assertEquals("Artista no encontrado con ID: 1", exception.getMessage());
        verify(artistRepository).findById(1L);
        verifyNoMoreInteractions(artistRepository);
    }
}