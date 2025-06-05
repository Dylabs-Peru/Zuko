package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateRoleRequest;
import com.dylabs.zuko.dto.response.RoleResponse;
import com.dylabs.zuko.exception.roleExeptions.RoleAlreadyExistesException;
import com.dylabs.zuko.exception.roleExeptions.RoleNotFoundException;
import com.dylabs.zuko.mapper.RoleMapper;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.repository.RoleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class RoleServiceUnitTest {
    private RoleRepository roleRepository;
    private RoleMapper roleMapper;
    private RoleService roleService;

    @BeforeEach
    void setUp() {
        roleRepository = mock(RoleRepository.class);
        roleMapper = mock(RoleMapper.class);
        roleService = new RoleService(roleRepository, roleMapper,);
    }

    @Test
    @DisplayName("CP01-HU21 - Crear rol correctamente")
    void createRole_whenRoleDoesNotExist_shouldCreateAndReturnRoleResponse() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "Administrador");
        Role roleEntity = new Role();
        Role savedRole = new Role(1L, "ADMIN", "Administrador");
        RoleResponse response = new RoleResponse(1L, "ADMIN", "Administrador");

        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(false);
        when(roleMapper.toRoleEntity(request)).thenReturn(roleEntity);
        when(roleRepository.save(roleEntity)).thenReturn(savedRole);
        when(roleMapper.toResponse(savedRole)).thenReturn(response);

        RoleResponse result = roleService.createRole(request);

        assertEquals(response, result);
    }

    @Test
    @DisplayName("CP02-HU21 - Crear rol cuando ya existe debe lanzar excepción")
    void createRole_whenRoleAlreadyExists_shouldThrowException() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "Administrador");

        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(true);

        assertThrows(RoleAlreadyExistesException.class, () -> roleService.createRole(request));
    }

    @Test
    @DisplayName("CP01-HU22 - Listar roles cuando hay registros debe devolver lista")
    void getRoles_whenRolesExist_shouldReturnListOfRoleResponses() {
        Role role = new Role(1L, "ADMIN", "Administrador");
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<RoleResponse> responses = roleService.getRoles();

        assertEquals(1, responses.size());
        assertEquals("ADMIN", responses.get(0).roleName());
    }

    @Test
    @DisplayName("CP02-HU22 - Listar roles cuando no hay registros debe lanzar excepción")
    void getRoles_whenNoRoles_shouldThrowException() {
        when(roleRepository.findAll()).thenReturn(List.of());

        assertThrows(RoleNotFoundException.class, () -> roleService.getRoles());
    }

    @Test
    @DisplayName("CP01-HU20 - Actualizar rol existente con datos válidos debe retornar el nuevo rol")
    void updateRole_whenValid_shouldUpdateAndReturnResponse() {
        long id = 1L;
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "Actualizado");
        Role existing = new Role(id, "ADMIN", "Antiguo");
        Role updated = new Role(id, "ADMIN", "Actualizado");
        RoleResponse response = new RoleResponse(id, "ADMIN", "Actualizado");

        when(roleRepository.findById(id)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(true);
        when(roleRepository.save(existing)).thenReturn(updated);
        when(roleMapper.toResponse(updated)).thenReturn(response);

        RoleResponse result = roleService.updateRole(id, request);

        assertEquals("Actualizado", result.description());
    }

    @Test
    @DisplayName("CP02-HU20 - Actualizar rol cuando no existe debe lanzar excepción")
    void updateRole_whenRoleNotFound_shouldThrowException() {
        when(roleRepository.findById(1L)).thenReturn(Optional.empty());

        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "desc");
        assertThrows(RoleNotFoundException.class, () -> roleService.updateRole(1L, request));
    }

    @Test
    @DisplayName("CP03-HU20 - Actualizar rol con nombre duplicado debe lanzar excepción")
    void updateRole_whenRoleNameExistsWithDifferentName_shouldThrowException() {
        Role existing = new Role(1L, "USER", "desc");
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "nuevo");

        when(roleRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(true);

        assertThrows(RoleAlreadyExistesException.class, () -> roleService.updateRole(1L, request));
    }

    @Test
    @DisplayName("CP01-HU19 - Eliminar rol existente debe borrarlo sin errores")
    void deleteRole_whenRoleExists_shouldDelete() {
        when(roleRepository.existsById(1L)).thenReturn(true);

        roleService.deleteRole(1L);

        verify(roleRepository).deleteById(1L);
    }

    @Test
    @DisplayName("CP02-HU19 - Eliminar rol cuando no existe debe lanzar excepción")
    void deleteRole_whenRoleNotFound_shouldThrowException() {
        when(roleRepository.existsById(1L)).thenReturn(false);

        assertThrows(RoleNotFoundException.class, () -> roleService.deleteRole(1L));
    }

}
