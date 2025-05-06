package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateUserRequest;
import com.dylabs.zuko.dto.response.UserResponse;
import com.dylabs.zuko.exception.userExeptions.UserAlreadyExistsException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.exception.roleExeptions.*;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.repository.UserRepository;
import com.dylabs.zuko.repository.RoleRepository;
import com.dylabs.zuko.mapper.UserMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUrl_image(),
                user.getDescription(),
                user.getUserRoleName()
        );
    }

    public UserResponse createUser(CreateUserRequest request) {

        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está en uso.");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("El correo electrónico ya está registrado.");
        }

        // 1. Verifica si el rol existe
        Role userRole = roleRepository.findByRoleNameIgnoreCase(request.roleName())
                .orElseThrow(() -> new RoleNotFoundException("El rol '" + request.roleName() + "' no existe."));

        // 2. Mapea el request a entidad
        User user = userMapper.toUserEntity(request);

        // 3. Asigna el rol
        user.setUserRole(userRole);

        // 4. Guarda y responde
        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    /// Crud

}
