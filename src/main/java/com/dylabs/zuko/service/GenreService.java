package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.GenreRequest;
import com.dylabs.zuko.model.Genre;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class GenreService {
    private List<Genre> genres = new ArrayList<Genre>();
    private long nextId = 1L;

    /// Registrar genero
    public Genre createGenre(GenreRequest request) {
        boolean exists = genres.stream().anyMatch(g->g.getName().equalsIgnoreCase(request.name()));
        if (exists) {
            return null;
        }
        boolean name_length_validation = request.name().length() < 3;
        boolean description_validation = request.description() != null && request.description().length() > 200;

        if (name_length_validation) {
            return null;
        }
        if (description_validation) {
            return null;
        }
        Genre newgenre = new Genre(
                nextId++, request.name(), request.description()
        );
        genres.add(newgenre);
        return newgenre;
    }

    /// Listar todos los géneros
    public List<Genre> getGenres() {
        return genres;
    }

    /// Editar un género

    public Genre updateGenre(long id, GenreRequest request) {
        Genre genre = genres.stream().filter(g->g.getId() == id).findFirst().orElse( null);
        if (genre != null) {
            if (request.name() == null || request.name().length() < 3) {
                return null;
            }
            boolean exists = genres.stream().anyMatch(g -> g.getId() !=id && g.getName().equalsIgnoreCase(request.name()));
            if (exists) {
                return null;
            }
            if (request.description() != null && request.description().length() > 200) {
                return null;
            }
            genre.setName(request.name());
            genre.setDescription(request.description());
            return genre;
        }
        else return null;
    }


    ///  Borrar un género
    public boolean deleteGenre(long id) {
        return genres.removeIf(genre -> genre.getId() == id);
    }

}
