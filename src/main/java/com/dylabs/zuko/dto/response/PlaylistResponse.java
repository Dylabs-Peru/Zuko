package com.dylabs.zuko.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record PlaylistResponse(
        Long playlistId,
        String name,
        String description,
        boolean isPublic,
        LocalDate createdAt,
        Set<SongResponse> songs,
        String url_image
) {}