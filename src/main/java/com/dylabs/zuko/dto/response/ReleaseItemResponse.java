package com.dylabs.zuko.dto.response;

import java.time.LocalDate;

public record ReleaseItemResponse(
        Long id,
        String title,
        String type, // "song" o "album"
        String artistName,
        String imageUrl,
        String youtubeUrl, // sólo para canciones
        LocalDate releaseDate
) {}

