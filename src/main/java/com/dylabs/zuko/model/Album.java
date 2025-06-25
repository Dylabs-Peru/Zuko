package com.dylabs.zuko.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "albums", uniqueConstraints = {
        @UniqueConstraint(columnNames = {"title", "artist_id"})
})
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private int releaseYear;

    private String cover;

    @ManyToOne(optional = false)
    @JoinColumn(name = "artist_id")
    private Artist artist;

    @OneToMany
    @JoinColumn(name = "album_id")
    private List<Song> songs = new ArrayList<>();

    @ManyToOne(optional = false)
    @JoinColumn(name = "genre_id")
    private Genre genre;

    private LocalDate releaseDate;

    private LocalDate creationDate;

    // Getters

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getReleaseYear() {
        return releaseYear;
    }

    public String getCover() {
        return cover;
    }

    public Artist getArtist() {
        return artist;
    }

    public List<Song> getSongs() {
        return songs;
    }

    public Genre getGenre() {
        return genre;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setReleaseYear(int releaseYear) {
        this.releaseYear = releaseYear;
    }

    public void setCover(String cover) {
        this.cover = cover;
    }

    public void setArtist(Artist artist) {
        this.artist = artist;
    }

    public void setSongs(List<Song> songs) {
        this.songs = songs;
    }

    public void setGenre(Genre genre) {
        this.genre = genre;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
}
