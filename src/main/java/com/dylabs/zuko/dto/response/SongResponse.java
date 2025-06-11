package com.dylabs.zuko.dto.response;

import java.time.LocalDate;

public record SongResponse(
        Long id,
        String title,
        boolean isPublicSong,
        LocalDate releaseDate,
        String message,
        Long artistId,
        String artistName
) {}
