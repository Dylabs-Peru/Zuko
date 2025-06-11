package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AddSongtoPlaylistRequest(
        @NotNull(message = "Es obligatorio añadir una canción")
        Long songId
) {
}
