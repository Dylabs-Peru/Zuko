package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthRequest(
        @NotBlank(message = "El JWT token de Google es requerido")
        String googleToken  // Cambiado: ahora es JWT token, no access token
) {
}