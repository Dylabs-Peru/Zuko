package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.GenreRequest;
import com.dylabs.zuko.dto.response.GenreResponse;
import com.dylabs.zuko.model.Genre;
import org.springframework.stereotype.Component;

@Component
public class GenreMapper {
    public GenreResponse toResponse(Genre genre) {
        if (genre == null) return null;
        return new GenreResponse(genre.getId(), genre.getName(), genre.getDescription());
    }
    public Genre toGenreEntity(GenreRequest request) {
        if (request == null) return null;
        return new Genre(request.name(), request.description());
    }
    public void updateGenreFromRequest(Genre genre, GenreRequest request) {
        genre.setName(request.name());
        genre.setDescription(request.description());
    }
}
