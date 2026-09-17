package com.hosteleriapractica.backendpractica.dto;

public record LoginResponse(
        String token,
        Long id,
        String username,
        String nombre,
        String rol
) {
}