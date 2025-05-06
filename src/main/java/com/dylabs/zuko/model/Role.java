package com.dylabs.zuko.model;


import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String roleName;

    @Column(nullable = false, length = 255)
    private String description;

    // Getters

    public String getName() {return roleName;}

    public Long getId() {
        return id;
    }

    public String getRoleName() {return roleName;
    }


    public String getDescription() {return description;}
    // Setters

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

}
