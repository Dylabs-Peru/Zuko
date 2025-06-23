package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.Size;

public record UpdatePlaylistRequest(
        @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        String name,
        String description,
        boolean isPublic,
        String url_image
) {}
