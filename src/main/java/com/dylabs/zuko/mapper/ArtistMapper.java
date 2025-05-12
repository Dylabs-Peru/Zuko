package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.CreateArtistRequest;
import com.dylabs.zuko.dto.response.ArtistResponse;
import com.dylabs.zuko.model.Artist;
import com.dylabs.zuko.model.User;
import org.springframework.stereotype.Component;

@Component
public class ArtistMapper {

    public Artist toEntity(CreateArtistRequest request, User user) {
        return Artist.builder()
                .name(request.name())
                .country(request.country())
                .biography(request.biography())
                .user(user)
                .build();
    }

    public ArtistResponse toResponse(Artist artist) {
        return new ArtistResponse(
                artist.getId(),
                artist.getName(),
                artist.getCountry(),
                artist.getBiography(),
                artist.getUser().getId()
        );
    }
}