package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.model.Song;
import org.springframework.stereotype.Component;

@Component
public class SongMapper {

    // Convertir una entidad Song a SongResponse
    public SongResponse toResponse(Song song) {
        if (song == null) return null;
        // Aquí no es necesario poner el mensaje de éxito, ya que puedes gestionarlo en el servicio.
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.isPublicSong(),
                song.getReleaseDate(),
                "Canción registrada exitosamente" // O un mensaje más específico si lo prefieres
        );
    }

    // Convertir un SongRequest a la entidad Song
    public Song toSongEntity(SongRequest request) {
        if (request == null) return null;
        return new Song(request.title(), request.isPublicSong());
    }
}