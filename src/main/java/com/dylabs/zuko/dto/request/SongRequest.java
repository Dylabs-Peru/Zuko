package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SongRequest(
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 3, message = "El título debe tener más de 3 caracteres")
        String title,

        boolean isPublicSong,
        String youtubeUrl,
        long artistId
) {}
