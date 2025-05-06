package com.dylabs.zuko.repository;
import com.dylabs.zuko.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;


public interface RoleRepository extends JpaRepository<Role, Long> {
    boolean existsByRoleNameIgnoreCase(String roleName);
    Optional<Role> findByRoleNameIgnoreCase(String roleName);
    Optional<Role> findById(long id);
    void deleteByRoleNameIgnoreCase(String roleName);
}
