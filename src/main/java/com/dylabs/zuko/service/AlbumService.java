package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.AlbumRequest;
import com.dylabs.zuko.dto.response.AlbumResponse;
import com.dylabs.zuko.exception.BadRequestException;
import com.dylabs.zuko.exception.ResourceNotFoundException;
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

    public AlbumResponse createAlbum(AlbumRequest request) {
        Long artistId = parseArtistId(request.artistId());

        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResourceNotFoundException("El artista no fue encontrado."));

        boolean exists = albumRepository.existsByTitleIgnoreCaseAndArtistId(request.title(), artistId);
        if (exists) {
            throw new BadRequestException("El título del álbum ya existe para este artista.");
        }

        List<Song> artistSongs = songRepository.findAllByArtistId(artistId);
        if (artistSongs.size() < 2) {
            throw new BadRequestException("Un álbum debe contener como mínimo 2 canciones.");
        }

        Album album = new Album();
        album.setTitle(request.title());
        album.setReleaseYear(request.releaseYear());
        album.setCover(request.cover());
        album.setArtist(artist);
        album.setSongs(artistSongs); // Asociamos todas las canciones del artista por ahora

        albumRepository.save(album);

        return new AlbumResponse(
                album.getId(),
                album.getTitle(),
                album.getReleaseYear(),
                album.getCover(),
                artist.getName(),
                album.getSongs().stream().map(Song::getTitle).toList()
        );
    }

    private Long parseArtistId(String artistIdString) {
        try {
            return Long.parseLong(artistIdString);
        } catch (NumberFormatException e) {
            throw new BadRequestException("ID de artista inválido.");
        }
    }
}
