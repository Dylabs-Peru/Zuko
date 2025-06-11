package com.dylabs.zuko.dto.response;

public record  UserResponse(
        Long id,
        String username,
        String email,
        String description,
        String url_image,
        String roleName, // El nombre del rol asignado
        boolean isActive
) {}
