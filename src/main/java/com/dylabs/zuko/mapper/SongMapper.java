package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    // Convertir una entidad Song a SongResponse
    public SongResponse toResponse(Song song) {
        if (song == null) return null;
        Long artistId = (song.getArtist() != null) ? song.getArtist().getId() : null;
        String artistName = (song.getArtist() != null) ? song.getArtist().getName() : null;

        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.isPublicSong(),
                song.getReleaseDate(),
                "Canción registrada exitosamente", // O personalizado desde el servicio
                artistId,
                artistName,
                song.getYoutubeUrl()
        );
    }
    // Convertir un SongRequest a la entidad Song
    public Song toSongEntity(SongRequest request, Artist artist) {
        Song song = new Song(request.title(), request.isPublicSong(), request.youtubeUrl());
        song.setArtist(artist);
        return song;
    }

}