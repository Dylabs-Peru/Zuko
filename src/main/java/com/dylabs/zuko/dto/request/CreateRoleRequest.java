package com.dylabs.zuko.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;


public record  CreateRoleRequest (
        @NotBlank(message="El nombre no puede estar vacío")
        @Size(min=3, message = "El nombre del rol debe tener al menos tres caracteres")
        String roleName,
        @Size(max=250,message = "La descripción no puede exceder 200 caracteres")
        String description
){
}
