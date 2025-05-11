package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.util.List;

public record AlbumRequest(
        @NotBlank(message = "El título es obligatorio")
        String title,

        @NotNull(message = "El año de lanzamiento es obligatorio")
        int releaseYear,

        String cover,

        @NotNull(message = "El ID del artista es obligatorio")
        Long artistId,

        @NotNull(message = "El ID del género es obligatorio")
        Long genreId,

        @NotNull(message = "El álbum debe contener al menos dos canciones")
        @Size(min = 2, message = "El álbum debe contener al menos dos canciones")
        List<@NotNull SongRequest> songs
) {}
