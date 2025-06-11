package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ArtistMapper {

    public Artist toEntity(CreateArtistRequest request, User user) {
        return Artist.builder()
                .name(request.name())
                .country(request.country())
                .biography(request.biography())
                .user(user)
                .isActive(true)
                .build();
    }

    public ArtistResponse toResponse(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getCountry(),
                artist.getBiography(),
                artist.getUser().getId(),
                artist.getIsActive()
        );
    }
    public List<ArtistResponse> toResponseList(List<Artist> artists) {
        return artists.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}