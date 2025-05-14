package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.Set;

public record PlaylistRequest(
        @NotBlank(message = "El nombre es obligatorio")
        @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        String name,

        String description,

        boolean isPublic,

        Set<Long> songIds, // IDs de canciones relacionadas
        Set<Long> userIds // IDs de usuarios relacionados
) {}