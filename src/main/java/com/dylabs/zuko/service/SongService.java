package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.SongRequest;
import com.dylabs.zuko.dto.response.SongResponse;
import com.dylabs.zuko.exception.artistExeptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.songExceptions.SongAlreadyExistException;
import com.dylabs.zuko.exception.songExceptions.SongNotFoundException;
import com.dylabs.zuko.mapper.SongMapper;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor

public class SongService {

    private final SongRepository repository;
    private final SongMapper songMapper;
    private final ArtistRepository artistRepository;

    // Crear canción
    public SongResponse createSong(SongRequest request) {

        // Buscar el artista por el ID
        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new ArtistNotFoundException("El artista con ID " + request.artistId() + " no existe"));

        // Validar si ya existe la canción con el mismo título y el mismo artista
        boolean exists = repository.existsByTitleIgnoreCaseAndArtistId(request.title(), request.artistId());
        if (exists) {
            throw new SongAlreadyExistException("La canción " + request.title() + " ya está registrada para este artista.");
        }

        // Crear una nueva canción
        Song song = songMapper.toSongEntity(request, artist);
        song.setReleaseDate(LocalDate.now()); // Aquí le asignas la fecha actual

        // Guardar la canción en la base de datos
        Song savedSong = repository.save(song);

        // Usamos el mapper para devolver la respuesta
        return songMapper.toResponse(savedSong);
    }


    // Editar canción
    public SongResponse updateSong(Long id, SongRequest request) {
        Song song = repository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("Canción no encontrada"));

        // Verificar si el nuevo título ya existe para el mismo artista
        boolean exists = repository.existsByTitleIgnoreCaseAndArtistId(request.title(), request.artistId());
        if (exists) {
            throw new SongAlreadyExistException("El título " + request.title() + " ya existe en el catálogo de este artista.");
        }

        // Actualizar los datos de la canción
        song.setTitle(request.title());
        song.setPublicSong(request.isPublicSong());

        // Buscar el artista por ID y asignarlo
        Artist artist = artistRepository.findById(request.artistId())
                .orElseThrow(() -> new ArtistNotFoundException("El artista con ID " + request.artistId() + " no existe"));
        song.setArtist(artist);

        Song updatedSong = repository.save(song);

        // Retornar la respuesta con mensaje de éxito
        return new SongResponse(
                updatedSong.getId(),
                updatedSong.getTitle(),
                updatedSong.isPublicSong(),
                updatedSong.getReleaseDate(),
                "La canción ha sido actualizada correctamente.",
                updatedSong.getArtist().getId(),
                updatedSong.getArtist().getName()
        );
    }

    // Eliminar canción
    public SongResponse deleteSong(Long id) {
        // Buscar la canción por id
        Song song = repository.findById(id)
                .orElseThrow(() -> new SongNotFoundException("La canción no se encontró en tu catálogo."));

        // Eliminar la canción
        repository.delete(song);

        // Crear y devolver la respuesta con mensaje de éxito
        return new SongResponse(
                song.getId(),
                song.getTitle(),
                song.isPublicSong(),
                song.getReleaseDate(),
                "La canción ha sido eliminada correctamente.",
                song.getArtist().getId(),
                song.getArtist().getName()
        );
    }
}