package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.ApiResponse;
import com.dylabs.zuko.dto.request.CreateUserRequest;
import com.dylabs.zuko.dto.request.LoginRequest;
import com.dylabs.zuko.dto.request.UpdateUserRequest;
import com.dylabs.zuko.dto.response.UserResponse;
import com.dylabs.zuko.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<ApiResponse<UserResponse>> createUser(@Valid @RequestBody CreateUserRequest request) {
        UserResponse response = userService.createUser(request);
        return new ResponseEntity<>(new ApiResponse<>("Usuario creado exitosamente", response), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponse> login(@Valid @RequestBody LoginRequest request) {
        UserResponse response = userService.login(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        List<UserResponse> users = userService.getAllUsers();
        return new ResponseEntity<>(users, HttpStatus.OK);
    }

    @PatchMapping("/{id}/toggle-active")
    public ResponseEntity<String> toggleUserActiveStatus(@PathVariable Long id) {
        userService.toggleUserActiveStatus(id);
        return ResponseEntity.ok("Estado de actividad del usuario actualizado correctamente.");
    }

    @GetMapping("/{id}")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        UserResponse response = userService.getUserById(id);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Long id,
                                                   @Valid @RequestBody UpdateUserRequest updateRequest) {
        UserResponse updatedUser = userService.updateUser(id, updateRequest);
        return new ResponseEntity<>(updatedUser, HttpStatus.OK);
    }
}
