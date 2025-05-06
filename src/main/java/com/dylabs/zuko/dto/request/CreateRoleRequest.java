package com.dylabs.zuko.dto.request;

import jdk.jfr.Percentage;

public record  CreateRoleRequest (
    @NotBlank(message = "Phone number cannot be blank / El número de celular no puede estar vacío")
    @Size()
    @Pettern(regexp = "^\\+?[0-9]+$", message = "Phone number must contain only digits and optionally start with '+' / El número de celular debe contener solo dígitos y opcionalmente empezar con '+'")
    String ,
){
}
