package com.dylabs.zuko.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "users")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column( nullable = false)
    private String password;

    @Column(unique = true, nullable = true)
    private String url_image;

    @Column(nullable = true)
    private String description;

    @ManyToOne
    @JoinColumn(name = "role_id", nullable = false)
    private Role userRole;

    @Column(nullable = false)
    private boolean isActive;


    /// getters

    public String getUsername(){return username;}

    public String getPassword(){return password;}

    public String getUrl_image(){return url_image;}

    public Long getId() {return id;}

    public String getDescription() {return description;}

    public String getEmail() {return email;}

    public String getUserRoleName() {
        return userRole != null ? userRole.getRoleName() : null;
    }
    public boolean getIsActive() {
        return isActive;
    }



    /// Setters

    public void setUsername(String username) {this.username = username;}

    public void setPassword(String password) {this.password = password;}

    public void setUrl_image(String url_image) {this.url_image = url_image;}

    public void setId(Long id) {this.id = id;}

    public void setDescription(String description) {this.description = description;}

    public void setEmail(String email) {this.email = email;}

    public void setUserRole(Role userRole) {
        this.userRole = userRole;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

}
