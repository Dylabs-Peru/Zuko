package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record CreateArtistRequest(

        @NotBlank(message = "El nombre del artista no puede estar vacío")
        @Size(min = 3, message = "El nombre del artista debe tener al menos 3 caracteres")
        String name,

        @NotBlank(message = "El país no puede estar vacío")
        String country,

        @Size(max = 1000, message = "La biografía no puede exceder 1000 caracteres")
        String biography


) {}
