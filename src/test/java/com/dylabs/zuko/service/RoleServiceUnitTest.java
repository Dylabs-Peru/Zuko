package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateRoleRequest;
import com.dylabs.zuko.dto.response.RoleResponse;
import com.dylabs.zuko.exception.roleExeptions.RoleAlreadyExistesException;
import com.dylabs.zuko.exception.roleExeptions.RoleNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.mapper.RoleMapper;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.RoleRepository;
import com.dylabs.zuko.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RoleServiceTest {

    @Mock private RoleRepository roleRepository;
    @Mock private RoleMapper roleMapper;
    @Mock private UserRepository userRepository;
    @InjectMocks private RoleService roleService;

    @Mock private SecurityContext securityContext;
    @Mock private Authentication authentication;

    private User adminUser;
    private User normalUser;

    @BeforeEach
    void setup() {
        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setUsername("admin");
        adminUser.setUserRoleName("Admin");

        normalUser = new User();
        normalUser.setId(2L);
        normalUser.setUsername("user");
        normalUser.setUserRoleName("User");

        when(securityContext.getAuthentication()).thenReturn(authentication);
        SecurityContextHolder.setContext(securityContext);
    }

    @Test
    void createRole_AsAdmin_Success() {
        CreateRoleRequest req = new CreateRoleRequest("MOD", "Moderador");
        Role role = new Role(1L, "MOD", "Moderador");
        RoleResponse response = new RoleResponse(1L, "MOD", "Moderador");

        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.existsByRoleNameIgnoreCase("MOD")).thenReturn(false);
        when(roleMapper.toRoleEntity(req)).thenReturn(role);
        when(roleRepository.save(role)).thenReturn(role);
        when(roleMapper.toResponse(role)).thenReturn(response);

        RoleResponse result = roleService.createRole(req);

        assertEquals("MOD", result.roleName());
    }

    @Test
    void createRole_AsNonAdmin_ThrowsAccessDenied() {
        when(authentication.getName()).thenReturn("2");
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));

        assertThrows(AccessDeniedException.class, () -> {
            roleService.createRole(new CreateRoleRequest("MOD", "desc"));
        });
    }

    @Test
    void createRole_AlreadyExists_ThrowsException() {
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.existsByRoleNameIgnoreCase("MOD"))
                .thenReturn(true);

        assertThrows(RoleAlreadyExistesException.class, () ->
                roleService.createRole(new CreateRoleRequest("MOD", "desc"))
        );
    }

    @Test
    void getRoles_Success() {
        Role role = new Role(1L, "ADMIN", "Administrador");
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findAll()).thenReturn(List.of(role));

        List<RoleResponse> result = roleService.getRoles();

        assertEquals(1, result.size());
    }

    @Test
    void getRoles_Empty_ThrowsException() {
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findAll()).thenReturn(Collections.emptyList());

        assertThrows(RoleNotFoundException.class, () -> roleService.getRoles());
    }

    @Test
    void getRoles_AsNonAdmin_ThrowsAccessDenied() {
        when(authentication.getName()).thenReturn("2");
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        assertThrows(AccessDeniedException.class, () -> roleService.getRoles());
    }

    @Test
    void updateRole_Success() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "Admin");
        Role role = new Role(1L, "MOD", "Mod");

        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(false);
        when(roleRepository.save(any())).thenReturn(role);
        when(roleMapper.toResponse(any())).thenReturn(new RoleResponse(1L, "ADMIN", "Admin"));

        RoleResponse response = roleService.updateRole(1L, request);

        assertEquals("ADMIN", response.roleName());
    }

    @Test
    void updateRole_AlreadyExistsWithDifferentName_ThrowsException() {
        CreateRoleRequest request = new CreateRoleRequest("ADMIN", "Admin");
        Role role = new Role(1L, "MOD", "Mod");

        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByRoleNameIgnoreCase("ADMIN")).thenReturn(true);

        assertThrows(RoleAlreadyExistesException.class, () ->
                roleService.updateRole(1L, request)
        );
    }

    @Test
    void updateRole_RoleNotFound_ThrowsException() {
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(99L)).thenReturn(Optional.empty());
        CreateRoleRequest req = new CreateRoleRequest("ADMIN", "Admin");
        assertThrows(RoleNotFoundException.class, () -> roleService.updateRole(99L, req));
    }

    @Test
    void updateRole_SameName_DoesNotThrow() {
        CreateRoleRequest request = new CreateRoleRequest("MOD", "Mod");
        Role role = new Role(1L, "MOD", "Mod");
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByRoleNameIgnoreCase("MOD")).thenReturn(true);
        when(roleRepository.save(any())).thenReturn(role);
        when(roleMapper.toResponse(any())).thenReturn(new RoleResponse(1L, "MOD", "Mod"));
        assertDoesNotThrow(() -> roleService.updateRole(1L, request));
    }

    @Test
    void updateRole_SameNameDifferentCase_DoesNotThrow() {
        CreateRoleRequest request = new CreateRoleRequest("mod", "Mod");
        Role role = new Role(1L, "MOD", "Mod");
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.findById(1L)).thenReturn(Optional.of(role));
        when(roleRepository.existsByRoleNameIgnoreCase("mod")).thenReturn(true);
        when(roleRepository.save(any())).thenReturn(role);
        when(roleMapper.toResponse(any())).thenReturn(new RoleResponse(1L, "mod", "Mod"));
        assertDoesNotThrow(() -> roleService.updateRole(1L, request));
    }

    @Test
    void deleteRole_Success() {
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.existsById(1L)).thenReturn(true);

        assertDoesNotThrow(() -> roleService.deleteRole(1L));
        verify(roleRepository, times(1)).deleteById(1L);
    }

    @Test
    void deleteRole_NotFound_ThrowsException() {
        when(authentication.getName()).thenReturn("1");
        when(userRepository.findById(1L)).thenReturn(Optional.of(adminUser));
        when(roleRepository.existsById(1L)).thenReturn(false);

        assertThrows(RoleNotFoundException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    void deleteRole_AsNonAdmin_ThrowsAccessDenied() {
        when(authentication.getName()).thenReturn("2");
        when(userRepository.findById(2L)).thenReturn(Optional.of(normalUser));
        assertThrows(AccessDeniedException.class, () -> roleService.deleteRole(1L));
    }

    @Test
    void getAuthenticatedUser_NotFound_ThrowsException() {
        when(authentication.getName()).thenReturn("99");
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(UserNotFoundExeption.class, () -> roleService.createRole(new CreateRoleRequest("R", "desc")));
    }
}
