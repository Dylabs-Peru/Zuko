package com.dylabs.zuko.model;

import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name="song")
public class Song {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String title;
    @Column(name = "is_public")
    private boolean isPublicSong;
    private LocalDate releaseDate;

    public Song() {}

    public Song(String title, boolean isPublicSong) {
        this.title = title;
        this.isPublicSong = isPublicSong;
        this.releaseDate = LocalDate.now(); //
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isPublicSong() {
        return isPublicSong;
    }

    public void setPublicSong(boolean publicSong) {
        isPublicSong = publicSong;
    }

    public LocalDate getReleaseDate() {
        return releaseDate;
    }

    public void setReleaseDate(LocalDate releaseDate) {
        this.releaseDate = releaseDate;
    }
}
