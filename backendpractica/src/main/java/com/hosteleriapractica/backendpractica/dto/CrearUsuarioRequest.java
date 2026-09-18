package com.hosteleriapractica.backendpractica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CrearUsuarioRequest(
        @NotBlank String username,
        @NotBlank @Size(min = 4, message = "La contraseña debe tener al menos 4 caracteres") String password,
        @NotBlank String nombre
) {
}