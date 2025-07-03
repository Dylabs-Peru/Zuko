package com.dylabs.zuko.dto.response;
import java.util.Set;

public record ShortcutsResponse(
        Long ShortcutsId,
        Set <PlaylistSummaryResponse> Playlists,
        Set <AlbumResponse> Albums // cambiar por otro adecuado
) {}
