package com.dylabs.zuko.dto.response;

import java.time.LocalDate;
import java.util.Set;

public record PlaylistResponse(
        Long playlistId,
        String name,
        String description,
        boolean isPublic,
        LocalDate createdAt,
        Set<SongResponse> songs, // Representar canciones con su información completa
        Set<Long> userIds // IDs de usuarios relacionados
) {}