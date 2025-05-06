package com.dylabs.zuko.controller;

import com.dylabs.zuko.dto.request.CreateRoleRequest;
import com.dylabs.zuko.dto.response.RoleResponse;
import com.dylabs.zuko.model.Role;
import com.dylabs.zuko.service.RoleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("roles")
@RequiredArgsConstructor
public class RoleController {
    private final RoleService roleService;

    ///  Definicion de endpoints

    @PostMapping
    public ResponseEntity<RoleResponse> creteRole(@Valid @RequestBody CreateRoleRequest request) {
        RoleResponse response = roleService.createRole(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<RoleResponse>> getAllRoles() {
        List<RoleResponse> roles = roleService.getRoles();
        return new ResponseEntity<>(roles, HttpStatus.OK);
    }

    @PutMapping("/{id}")
    public ResponseEntity<RoleResponse> updateRole(@PathVariable long id, @Valid @RequestBody CreateRoleRequest request) {
        RoleResponse updatedRole = roleService.updateRole(id, request);
        return new ResponseEntity<>(updatedRole, HttpStatus.OK);
    }

    @DeleteMapping("/{name}")
    public ResponseEntity<Void> deleteRole(@PathVariable String name) {
        roleService.deleteRole(name);
        return ResponseEntity.ok().build();
    }
}
