package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Método para encontrar un usuario por su nombre de usuario (username)
    Optional<User> findByUsername(String username);
    Optional<User> findByEmailIgnoreCase(String email);
    // Método para encontrar un usuario por su correo electrónico
    Optional<User> findByEmail(String email);
}
