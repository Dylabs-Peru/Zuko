package com.dylabs.zuko.dto.response;

public record AlbumResponse(
        Long id,
        String title,
        int releaseYear,
        String cover,
        Long artistId
) {}
