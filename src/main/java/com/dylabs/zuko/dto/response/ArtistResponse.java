package com.dylabs.zuko.dto.response;

public record ArtistResponse(
        Long id,
        String name,
        String country,
        String biography,
        Long userId,
        boolean isActive
) {}