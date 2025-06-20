package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.GenreRequest;
import com.dylabs.zuko.dto.response.GenreResponse;
import com.dylabs.zuko.exception.genreExeptions.GenreAlreadyExistsException;
import com.dylabs.zuko.exception.genreExeptions.GenreInUseException;
import com.dylabs.zuko.exception.genreExeptions.GenreNotFoundException;
import com.dylabs.zuko.mapper.GenreMapper;
import com.dylabs.zuko.model.Genre;
import com.dylabs.zuko.repository.GenreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import com.dylabs.zuko.repository.AlbumRepository;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GenreService {
    private final GenreRepository repository;
    private final AlbumRepository albumRepository;
    private final GenreMapper mapper;

    private GenreResponse toResponse(Genre genre){
        return new GenreResponse(genre.getId(), genre.getName(), genre.getDescription());
    }
    /// Registrar genero
    public GenreResponse createGenre(GenreRequest request) {
        boolean exists = repository.existsByNameIgnoreCase(request.name());
        if (exists) {
            throw new GenreAlreadyExistsException("El genero " + request.name() + " ya está registrado");
        }
        Genre newGenre = mapper.toGenreEntity(request);
        Genre savedGenre = repository.save(newGenre);
        return mapper.toResponse(savedGenre);

    }

    /// Listar todos los géneros
    public List<GenreResponse> getGenres() {
        List<Genre> genreList = repository.findAll();
        if (genreList.isEmpty()) {
            throw new GenreNotFoundException("Aún no se han registrado géneros musicales.");
        }
        return genreList.stream().map(this::toResponse).toList();
    }


    /// Editar un género

    public GenreResponse updateGenre(long id, GenreRequest request) {
        Genre genre = repository.findById(id).orElseThrow(() -> new GenreNotFoundException("El género con id " + id + " no existe"));

        boolean exists = repository.existsByNameIgnoreCase(request.name());
        if (exists && !genre.getName().equalsIgnoreCase(request.name())) {
            throw new GenreAlreadyExistsException("El genero " + request.name() + " ya está registrado");
        }
        genre.setName(request.name());
        genre.setDescription(request.description());
        Genre updatedGenre = repository.save(genre);
        return mapper.toResponse(updatedGenre);
    }


    ///  Borrar un género
    public void deleteGenre(long id) {
        if (!repository.existsById(id)) {
            throw new GenreNotFoundException("El género con id " + id + " no existe");
        }

        if (albumRepository.existsByGenreId(id)) {
            throw new GenreInUseException("No se puede eliminar el género porque está asociado a uno o más álbumes.");
        }
        repository.deleteById(id);
    }

}