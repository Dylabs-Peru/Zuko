package com.dylabs.zuko.dto.response;

public record PlaylistSummaryResponse(
        Long playlistId,
        String name,
        String urlImage,
        String owner

)
{}
