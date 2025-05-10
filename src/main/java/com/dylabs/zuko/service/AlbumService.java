package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.albumExceptions.AlbumAlreadyExistsException;
import com.dylabs.zuko.exception.albumExceptions.AlbumNotFoundException;
import com.dylabs.zuko.exception.artistExceptions.ArtistNotFoundException;
import com.dylabs.zuko.exception.commonExceptions.BadRequestException;
import com.dylabs.zuko.mapper.AlbumMapper;
import com.dylabs.zuko.model.Album;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.Song;
import com.dylabs.zuko.repository.AlbumRepository;
import com.dylabs.zuko.repository.ArtistRepository;
import com.dylabs.zuko.repository.SongRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;
    private final ArtistRepository artistRepository;
    private final SongRepository songRepository;
    private final AlbumMapper albumMapper;

    public AlbumResponse createAlbum(AlbumRequest request) {
        Long artistId = parseArtistId(request.artistId());

        // Verificar existencia del artista
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ArtistNotFoundException("El artista no fue encontrado."));

        // Verificar duplicado por título
        boolean exists = albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artistId);
        if (exists) {
            throw new AlbumAlreadyExistsException("El título del álbum ya existe para este artista.");
        }

        // Verificar que el artista tenga al menos 2 canciones
        List<Song> artistSongs = songRepository.findAllByArtistId(artistId);
        if (artistSongs.size() < 2) {
            throw new BadRequestException("Un álbum debe contener como mínimo 2 canciones.");
        }

        // Crear entidad Album
        Album album = albumMapper.toAlbumEntity(request, artist);
        album.setSongs(artistSongs); // Asociar canciones del artista

        albumRepository.save(album);

        return albumMapper.toResponse(album);
    }

    private Long parseArtistId(String artistIdString) {
        try {
            return Long.parseLong(artistIdString);
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID de artista inválido.");
        }
    }
}
