package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.SongAlreadyExistException;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class SongService {
    private final SongRepository repository;
    private final SongMapper songMapper; // Usar el mapper para convertir la entidad a DTO

    /// Crear canción
    public SongResponse createSong(SongRequest request) {
        // Validación extra (si lo necesitas)
        validateSongRequest(request);

        // Validar si ya existe la canción con el mismo título
        boolean exists = repository.existsByTitleIgnoreCase(request.title());
        if (exists) {
            throw new SongAlreadyExistException("La canción " + request.title() + " ya está registrada.");
        }

        // Crear una nueva canción
        Song song = songMapper.toSongEntity(request);
        song.setReleaseDate(LocalDate.now()); // Aquí le asignas la fecha actual

        // Guardar la canción en la base de datos
        Song savedSong = repository.save(song);

        // Usamos el mapper para devolver la respuesta
        return songMapper.toResponse(savedSong);
    }

    // Validaciones adicionales
    private void validateSongRequest(SongRequest request) {
        if (request.title() == null || request.title().isBlank()) {
            throw new IllegalArgumentException("El título de la canción es obligatorio.");
        }

        if (request.title().length() <= 3) {
            throw new IllegalArgumentException("El título debe tener más de 3 caracteres.");
        }
    }
}