package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GenreRequest(
        @NotBlank(message="El nombre no puede estar vacío")
        @Size(min=3, message = "El nombre del género debe tener al menos tres caracteres")
        String name,
        @Size(max=200,message = "La descripción no puede exceder 200 caracteres")
        String description
){}
