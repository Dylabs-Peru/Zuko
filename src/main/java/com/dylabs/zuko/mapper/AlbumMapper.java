package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import org.springframework.stereotype.Component;

@Component
public class AlbumMapper {

    // Convertir AlbumRequest a la entidad Album
    public Album toAlbumEntity(AlbumRequest request, Artist artist) {
        if (request == null) return null;
        Album album = new Album();
        album.setTitle(request.titulo());
        album.setReleaseYear(request.anioLanzamiento());
        album.setCover(request.portada());
        album.setArtist(artist);
        return album;
    }

    // Convertir la entidad Album a un AlbumResponse
    public AlbumResponse toResponse(Album album) {
        if (album == null) return null;
        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                album.getArtist().getId()
        );
    }
}
