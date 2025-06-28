package com.dylabs.zuko.dto.response;

import java.time.LocalDate;

public record AlbumSongSummaryResponse(
        String title,
        LocalDate releaseDate
) {}
