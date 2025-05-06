package com.dylabs.zuko.repository;

import com.dylabs.zuko.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RoleRepository extends JpaRepository<Role,Long> {
    boolean existsByNameIgnoreCase(String name);
    Optional<Role> findByNameIgnoreCase(String name);
    Optional<Role> findById(long id);
    boolean existsById(long id);
    void deleteById(long id);
}
