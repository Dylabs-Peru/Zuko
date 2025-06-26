package com.dylabs.zuko.model;
import jakarta.persistence.*;
import java.util.HashSet;
import java.util.Set;
@Entity
@Table
public class Shortcuts {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "shortcuts_playlists",
            joinColumns = @JoinColumn(name = "shortcuts_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id")

    )
    private Set<Playlist> playlists = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "shortcuts_playlists",
            joinColumns = @JoinColumn(name = "shortcuts_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")

    )
    private Set<Album> albums = new HashSet<>();



}
