package com.dylabs.zuko.model;

import jakarta.persistence.*;

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
    @JoinColumn(name = "artist_id", nullable = false)
    private Artist artist;

    @OneToMany
    @JoinColumn(name = "album_id")
    private List<Song> songs;

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
}
