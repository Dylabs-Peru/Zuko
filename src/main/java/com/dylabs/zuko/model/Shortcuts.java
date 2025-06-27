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
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @ManyToMany
    @JoinTable(
            name = "shortcuts_playlists",
            joinColumns = @JoinColumn(name = "shortcuts_id"),
            inverseJoinColumns = @JoinColumn(name = "playlist_id", foreignKey = @ForeignKey(name = "fk_playlist", foreignKeyDefinition = "FOREIGN KEY (playlist_id) REFERENCES playlists(playlist_id) ON DELETE CASCADE"))

    )
    private Set<Playlist> playlists = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "shortcuts_albums",
            joinColumns = @JoinColumn(name = "shortcuts_id"),
            inverseJoinColumns = @JoinColumn(name = "album_id")
            // aplicar casacade aqui tmb

    )
    private Set<Album> albums = new HashSet<>();


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Set<Playlist> getPlaylists() {
        return playlists;
    }

    public void setPlaylists(Set<Playlist> playlists) {
        this.playlists = playlists;
    }

    public Set<Album> getAlbums() {
        return albums;
    }

    public void setAlbums(Set<Album> albums) {
        this.albums = albums;
    }
}
