package com.dylabs.zuko.mapper;

import com.dylabs.zuko.dto.request.CreateUserRequest;
import com.dylabs.zuko.dto.response.UserResponse;
import com.dylabs.zuko.model.User;
import com.dylabs.zuko.model.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    // Convertir User a UserResponse (para devolver la información del usuario)
    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return new UserResponse(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getDescription(),
                user.getUrl_image(),
                user.getUserRoleName(),
                user.getIsActive()
        );
    }

    // Convertir CreateUserRequest a User (para crear un usuario)
    public User toUserEntity(CreateUserRequest request) {
        if (request == null) return null;
        return User.builder()
                .username(request.username())
                .email(request.email())
                .password(request.password())
                .url_image(request.url_image())
                .description(request.description())
                .build();
    }
}
