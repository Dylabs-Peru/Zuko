package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;

public record GoogleOAuthRequest(
        @NotBlank(message = "El token de Google es requerido")
        String googleToken
) {
}
