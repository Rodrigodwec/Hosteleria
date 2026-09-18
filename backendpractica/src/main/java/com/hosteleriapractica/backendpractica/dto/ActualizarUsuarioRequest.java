package com.hosteleriapractica.backendpractica.dto;

import jakarta.validation.constraints.NotBlank;

public record ActualizarUsuarioRequest(
        @NotBlank String nombre,
        String password,
        boolean activo
) {
}