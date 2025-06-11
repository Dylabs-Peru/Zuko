package com.dylabs.zuko.service;

import com.dylabs.zuko.dto.request.CreateUserRequest;
import com.dylabs.zuko.dto.request.LoginRequest;
import com.dylabs.zuko.dto.request.UpdateUserRequest;
import com.dylabs.zuko.dto.response.AuthResponse;
import com.dylabs.zuko.dto.response.UserResponse;
import com.dylabs.zuko.exception.userExeptions.IncorretPasswordExeption;
import com.dylabs.zuko.exception.userExeptions.UserAlreadyExistsException;
import com.dylabs.zuko.exception.userExeptions.UserNotFoundExeption;
import com.dylabs.zuko.exception.roleExeptions.*;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.repository.UserRepository;
import com.dylabs.zuko.repository.RoleRepository;
import com.dylabs.zuko.mapper.UserMapper;
import com.dylabs.zuko.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final RoleRepository roleRepository;

    @Autowired
    private AuthenticationManager authManager;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getUrl_image(),
                user.getDescription(),
                user.getUserRoleName(),
                user.getIsActive()
        );
    }


    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.findByUsername(request.username()).isPresent()) {
            throw new UserAlreadyExistsException("El nombre de usuario ya está en uso.");
        }

        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new UserAlreadyExistsException("El correo electrónico ya está registrado.");
        }

        String roleName = (request.roleName() != null) ? request.roleName() : "User";
        Role userRole = roleRepository.findByRoleNameIgnoreCase(roleName)
                .orElseThrow(() -> new RoleNotFoundException("El rol '" + roleName + "' no existe."));

        User user = userMapper.toUserEntity(request);
        user.setUserRole(userRole);

        user.setPassword(passwordEncoder.encode(request.password()));

        user.setActive(request.isActive() == null ? true : request.isActive());

        User savedUser = userRepository.save(user);
        return userMapper.toResponse(savedUser);
    }

    public AuthResponse login(LoginRequest request) {
        try {
            authManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email(), request.password())
            );
        } catch (BadCredentialsException e) {
            throw new IncorretPasswordExeption("Usuario o contraseña incorrectos.");
        }

        User user = userRepository.findByEmailIgnoreCase(request.email())
                .orElseThrow(() -> new UserNotFoundExeption("No existe un usuario con ese correo"));

        if (!user.getIsActive()) {
            throw new UserNotFoundExeption("El usuario está desactivado.");
        }

        String token = JwtUtil.generateToken(user.getId().toString(), user.getUserRoleName());
        return new AuthResponse(token);
    }


    public List<UserResponse> getAllUsers() {
        List<User> users = userRepository.findAll();
        return users.stream().map(userMapper::toResponse).toList();
    }

//    public void toggleUserActiveStatus(Long id) {
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new UserNotFoundExeption("Usuario con ID " + id + " no encontrado"));
//
//        user.setActive(!user.getIsActive());
//        userRepository.save(user);
//    }

    public void toggleUserActiveStatus(Long id) {
        User currentUser = getAuthenticatedUser();

        if (!currentUser.getUserRoleName().equalsIgnoreCase("Admin")) {
            throw new AccessDeniedException("Solo los administradores pueden cambiar el estado de un usuario.");
        }

        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundExeption("Usuario con ID " + id + " no encontrado"));

        user.setActive(!user.getIsActive());
        userRepository.save(user);
    }


    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundExeption("Usuario con ID " + id + " no encontrado"));
        return userMapper.toResponse(user);
    }

//    public UserResponse updateUser(Long id, UpdateUserRequest updateRequest) {
//
//
//        User user = userRepository.findById(id)
//                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + id));
//
//
//        // Check for duplicate username
//        if (updateRequest.username() != null) {
//            Optional<User> existingUser = userRepository.findByUsername(updateRequest.username());
//            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
//                throw new UserAlreadyExistsException("El nombre de usuario ya está en uso.");
//            }
//            user.setUsername(updateRequest.username());
//        }
//
//        // Check for duplicate email
//        if (updateRequest.email() != null) {
//            Optional<User> existingUser = userRepository.findByEmail(updateRequest.email());
//            if (existingUser.isPresent() && !existingUser.get().getId().equals(user.getId())) {
//                throw new UserAlreadyExistsException("El correo electrónico ya está registrado.");
//            }
//            user.setEmail(updateRequest.email());
//        }
//
//        // Update other fields
//        if (updateRequest.description() != null) {
//            user.setDescription(updateRequest.description());
//        }
//        if (updateRequest.url_image() != null) {
//            user.setUrl_image(updateRequest.url_image());
//        }
//        if (updateRequest.password() != null) {
//            user.setPassword(updateRequest.password());
//        }
//
//        User updatedUser = userRepository.save(user);
//
//        return new UserResponse(
//                updatedUser.getId(),
//                updatedUser.getUsername(),
//                updatedUser.getEmail(),
//                updatedUser.getDescription(),
//                updatedUser.getUrl_image(),
//                updatedUser.getUserRoleName(),
//                updatedUser.getIsActive()
//        );
//    }

    public UserResponse updateUser(Long id, UpdateUserRequest updateRequest) {
        User userToUpdate = userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundExeption("Usuario no encontrado con id: " + id));

        User currentUser = getAuthenticatedUser();

        boolean isAdmin = currentUser.getUserRoleName().equalsIgnoreCase("Admin");

        // Si no es admin y no es su propio perfil => prohibido
        if (!isAdmin && !userToUpdate.getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("No puedes editar a otros usuarios.");
        }

        // Validación extra para evitar que un user cambie su propio rol
        if (!isAdmin && updateRequest.roleName() != null) {
            throw new AccessDeniedException("No tienes permiso para cambiar el rol.");
        }

        // Check for duplicate username
        if (updateRequest.username() != null) {
            Optional<User> existingUser = userRepository.findByUsername(updateRequest.username());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userToUpdate.getId())) {
                throw new UserAlreadyExistsException("El nombre de usuario ya está en uso.");
            }
            userToUpdate.setUsername(updateRequest.username());
        }

        // Check for duplicate email
        if (updateRequest.email() != null) {
            Optional<User> existingUser = userRepository.findByEmail(updateRequest.email());
            if (existingUser.isPresent() && !existingUser.get().getId().equals(userToUpdate.getId())) {
                throw new UserAlreadyExistsException("El correo electrónico ya está registrado.");
            }
            userToUpdate.setEmail(updateRequest.email());
        }

        // Otros campos
        if (updateRequest.description() != null) {
            userToUpdate.setDescription(updateRequest.description());
        }

        if (updateRequest.url_image() != null) {
            userToUpdate.setUrl_image(updateRequest.url_image());
        }

        if (updateRequest.password() != null) {
            userToUpdate.setPassword(passwordEncoder.encode(updateRequest.password()));
        }

        // Solo los admins pueden cambiar el rol
        if (isAdmin && updateRequest.roleName() != null) {
            Role newRole = roleRepository.findByRoleNameIgnoreCase(updateRequest.roleName())
                    .orElseThrow(() -> new RoleNotFoundException("Rol no encontrado: " + updateRequest.roleName()));
            userToUpdate.setUserRole(newRole);
        }

        User updatedUser = userRepository.save(userToUpdate);
        return userMapper.toResponse(updatedUser);
    }


    // Metodo Helper

    private User getAuthenticatedUser() {
        String userId = SecurityContextHolder.getContext().getAuthentication().getName(); // que ahora será el ID
        return userRepository.findById(Long.parseLong(userId))
                .orElseThrow(() -> new UserNotFoundExeption("Usuario autenticado no encontrado"));
    }


}
