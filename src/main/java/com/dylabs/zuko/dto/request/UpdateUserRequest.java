package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Email;

public record UpdateUserRequest(
        @Size(min = 3, message = "El nombre de usuario debe tener al menos tres caracteres")
        String username,

        @Email(message = "El email debe ser válido")
        String email,

        @Size(max = 255, message = "La descripción no puede exceder los 255 caracteres")
        String description,

        String url_image,

        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+={}|\\[\\]\\\\:;\"'<>,.?/-]{6,}$",
                message = "La contraseña debe contener al menos una letra, un número y puede contener símbolos.")
        String password,

        String roleName // El nombre del rol asignado
) {}
