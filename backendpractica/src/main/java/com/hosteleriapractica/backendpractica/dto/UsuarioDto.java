package com.hosteleriapractica.backendpractica.dto;

import com.hosteleriapractica.backendpractica.model.Usuario;

public record UsuarioDto(
        Long id,
        String username,
        String nombre,
        String rol,
        boolean activo
) {
    public static UsuarioDto from(Usuario usuario) {
        return new UsuarioDto(
                usuario.getId(),
                usuario.getUsername(),
                usuario.getNombre(),
                usuario.getRol().name(),
                usuario.isActivo()
        );
    }
}