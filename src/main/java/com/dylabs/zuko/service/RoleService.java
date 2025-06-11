package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateRoleRequest;
import com.dylabs.zuko.exception.roleExeptions.RoleAlreadyExistesException;
import com.dylabs.zuko.exception.roleExeptions.RoleNotFoundException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.dto.response.RoleResponse;
import com.dylabs.zuko.mapper.RoleMapper;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.repository.RoleRepository;
import com.dylabs.zuko.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;
    private final UserRepository userRepository;

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    private User getAuthenticatedUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario autenticado no encontrado"));
    }

    private void checkIfAdmin() {
        User currentUser = getAuthenticatedUser();
        if (!currentUser.getUserRoleName().equalsIgnoreCase("Admin")) {
            throw new AccessDeniedException("No tienes permisos para realizar esta acción");
        }
    }


    /// Crear rol
    public RoleResponse createRole(CreateRoleRequest roleRequest) {
        checkIfAdmin();
        boolean exists = roleRepository.existsByRoleNameIgnoreCase(roleRequest.roleName());
        if (exists) {
            throw new RoleAlreadyExistesException("El rol " + roleRequest.roleName() + " ya está registrado");
        }
        Role newRole = roleMapper.toRoleEntity(roleRequest);
        Role savedRole = roleRepository.save(newRole);
        return roleMapper.toResponse(savedRole);
    }

    ///  Listar todos los roles
    public List<RoleResponse> getRoles() {
        checkIfAdmin();
        List<Role> roleList = roleRepository.findAll();
        if (roleList.isEmpty()) {
            throw new RoleNotFoundException("Aún no se han registrado roles.");
        }
        return roleList.stream().map(this::toResponse).toList();
    }

    /// Editar un rol
    public RoleResponse updateRole(long id, CreateRoleRequest roleRequest) {
        checkIfAdmin();
        Role role = roleRepository.findById(id).orElseThrow(() -> new RoleNotFoundException("El rol con id " + id + " no existe"));

        boolean exists = roleRepository.existsByRoleNameIgnoreCase(roleRequest.roleName());
        if (exists && !role.getName().equalsIgnoreCase(roleRequest.roleName())) {
            throw new RoleAlreadyExistesException("El rol " + roleRequest.roleName() + " ya está registrado");
        }
        role.setRoleName(roleRequest.roleName());
        role.setDescription(roleRequest.description());
        Role updatedRole = roleRepository.save(role);
        return roleMapper.toResponse(updatedRole);
    }

    /// Borrar un rol
    public void deleteRole(long id) {
        checkIfAdmin();
        if(!roleRepository.existsById(id)) {
            throw new RoleNotFoundException("El rol " + id + " no existe");
        }
        roleRepository.deleteById(id);
    }

}
