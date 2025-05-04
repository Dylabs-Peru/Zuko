package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.GenreRequest;
import com.dylabs.zuko.dto.response.GenreResponse;
import com.dylabs.zuko.exception.GenreAlreadyExistsException;
import com.dylabs.zuko.exception.GenreNotFoundException;
import com.dylabs.zuko.model.Genre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class GenreService {
    private List<Genre> genres = new ArrayList<Genre>();
    private long nextId = 1L;

    private GenreResponse toResponse(Genre genre){
        return new GenreResponse(genre.getId(), genre.getName(), genre.getDescription());
    }
    /// Registrar genero
    public GenreResponse createGenre(GenreRequest request) {
        boolean exists = genres.stream().anyMatch(g->g.getName().equalsIgnoreCase(request.name()));
        if (exists) {
            throw new GenreAlreadyExistsException("El genero " + request.name() + " ya está registrado");
        }

        Genre newGenre = new Genre(
                nextId++, request.name(), request.description()
        );
        genres.add(newGenre);
        return toResponse(newGenre);
    }

    /// Listar todos los géneros
    public List<GenreResponse> getGenres() {
        return genres.stream().map(this::toResponse).toList();
    }

    /// Editar un género

    public GenreResponse updateGenre(long id, GenreRequest request) {
        Genre genre = genres.stream().filter(g->g.getId() == id).findFirst().
                orElseThrow(() -> new GenreNotFoundException("El género con id " + id + "no  existe"));
        boolean exists = genres.stream().anyMatch(g -> g.getId() !=id && g.getName().equalsIgnoreCase(request.name()));
        if (exists) {
            throw new GenreAlreadyExistsException("El genero " + request.name() + " ya está registrado");
        }
        genre.setName(request.name());
        genre.setDescription(request.description());
        return toResponse(genre);
    }


    ///  Borrar un género
    public boolean deleteGenre(long id) {
        return genres.removeIf(genre -> genre.getId() == id);
    }

}
