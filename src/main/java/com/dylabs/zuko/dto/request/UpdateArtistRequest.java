package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.Size;

public record UpdateArtistRequest(
        @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
        String name,

        String country,

        @Size(max = 1000, message = "La biografía no puede tener más de 1000 caracteres")
        String biography
) {}
