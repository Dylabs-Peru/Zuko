package com.dylabs.zuko.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "artists")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String country;

    @Column(length = 1000)
    private String biography;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(nullable = false, columnDefinition = "boolean default true")
    private Boolean isActive= true;

    // Getters

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCountry() {
        return country;
    }

    public String getBiography() {
        return biography;
    }

    public User getUser() {
        return user;
    }

    public Boolean getIsActive() { return isActive; }

    // Setters

    public void setId(Long id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public void setBiography(String biography) {
        this.biography = biography;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public void setIsActive(Boolean isActive) { this.isActive = isActive; }
}
