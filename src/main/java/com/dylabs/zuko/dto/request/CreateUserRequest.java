package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record CreateUserRequest(
        @NotBlank(message = "El nombre de usuario no puede estar vacío")
        @Size(min = 3, message = "El nombre de usuario debe tener al menos tres caracteres")
        String username,

        @NotBlank(message = "El correo electrónico no puede estar vacío")
        @Email(message = "El correo electrónico debe ser válido")
        String email,

        @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d!@#$%^&*()_+={}|\\[\\]\\\\:;\"'<>,.?/-]{6,}$",
                message = "La contraseña debe contener al menos una letra, un número y puede contener símbolos.")
        @NotBlank(message = "La contraseña no puede estar vacía")
        @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
        String password,

        String url_image,

        @Size(max = 250, message = "La descripción no puede exceder 250 caracteres")
        String description,

        String roleName,

        Boolean isActive

) {}
