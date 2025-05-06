package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateRoleRequest;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.dto.response.RoleResponse;
import com.dylabs.zuko.mapper.RoleMapper;
import com.dylabs.zuko.repository.RoleRepository;
import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoleService {
    private final RoleRepository roleRepository;
    private final RoleMapper roleMapper;

    private RoleResponse toResponse(Role role) {
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    /// Crear rol
    public RoleResponse createRole(CreateRoleRequest roleRequest) {
        boolean exists = roleRepository.existsByNameIgnoreCase(roleRequest.roleName());
        if (exists) {
            throw new IllegalArgumentException("El rol " + roleRequest.roleName() + " ya está registrado");
        }
        Role newRole = roleMapper.toRoleEntity(roleRequest);
        Role savedRole = roleRepository.save(newRole);
        return roleMapper.toResponse(savedRole);
    }

    ///  Listar todos los roles
    public List<RoleResponse> getAllRoles() {
        List<Role> roleList = roleRepository.findAll();
        if (roleList.isEmpty()) {
            throw new IllegalArgumentException("Aún no se han registrado roles.");
        }
        return roleList.stream().map(this::toResponse).toList();
    }
}
