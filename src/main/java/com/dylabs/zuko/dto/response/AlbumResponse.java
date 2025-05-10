package com.dylabs.zuko.dto.response;

import java.util.List;

public record AlbumResponse(
        Long id,
        String title,
        int releaseYear,
        String cover,
        String artistName,
        List<String> songTitles
) {}
