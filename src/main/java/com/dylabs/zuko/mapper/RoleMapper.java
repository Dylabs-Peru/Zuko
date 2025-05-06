package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.response.RoleResponse;
import com.dylabs.zuko.dto.request.CreateRoleRequest;
import com.dylabs.zuko.model.Role;
import org.springframework.stereotype.Component;

@Component
public class RoleMapper {
    public RoleResponse toResponse(Role role) {
        if (role == null) return null;
        return new RoleResponse(role.getId(), role.getName(), role.getDescription());
    }

    public Role toRoleEntity(CreateRoleRequest request) {
        if (request == null) return null;
        return new Role();
    }

    public void updateRoleFromRequest(Role role, CreateRoleRequest request) {
        role.setRoleName(request.roleName());
        role.setDescription(request.description());
    }
}
