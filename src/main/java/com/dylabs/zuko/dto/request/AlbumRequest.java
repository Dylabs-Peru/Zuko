package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AlbumRequest(
        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotNull(message = "El año de lanzamiento es obligatorio")
        int releaseYear,

        String cover,

        @NotBlank(message = "El ID del artista es obligatorio")
        String artistId
) {}
