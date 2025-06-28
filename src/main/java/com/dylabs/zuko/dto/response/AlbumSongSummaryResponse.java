package com.dylabs.zuko.dto.response;

import java.time.LocalDate;

public record AlbumSongSummaryResponse(
        Long id,
        String title,
        LocalDate releaseDate,
        String youtubeUrl
) {}

