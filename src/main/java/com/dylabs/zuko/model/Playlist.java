package com.dylabs.zuko.model;

import java.time.LocalDate;

public class Playlist {
    private Long id;
    private String name;
    private boolean isPublic;
    private LocalDate createdAt;
    private String description;

    public Playlist(Long id, String name, boolean isPublic, LocalDate createdAt, String description) {
        this.id = id;
        this.name = name;
        this.isPublic = isPublic;
        this.createdAt = createdAt;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isPublic() {
        return isPublic;
    }

    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    public LocalDate getCreatedAt() {
        return createdAt;
    }
}
