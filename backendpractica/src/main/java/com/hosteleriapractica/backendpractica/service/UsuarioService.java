package com.hosteleriapractica.backendpractica.service;

import com.hosteleriapractica.backendpractica.dto.ActualizarUsuarioRequest;
import com.hosteleriapractica.backendpractica.dto.CrearUsuarioRequest;
import com.hosteleriapractica.backendpractica.exception.ApiException;
import com.hosteleriapractica.backendpractica.model.Rol;
import com.hosteleriapractica.backendpractica.model.Usuario;
import com.hosteleriapractica.backendpractica.repository.UsuarioRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioService(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Usuario> listarCamareros() {
        return usuarioRepository.findAll().stream()
                .filter(u -> u.getRol() == Rol.CAMARERO)
                .toList();
    }

    public Usuario obtener(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> ApiException.notFound("Usuario no encontrado"));
    }

    @Transactional
    public Usuario crearCamarero(CrearUsuarioRequest request) {
        if (usuarioRepository.existsByUsername(request.username())) {
            throw ApiException.conflict("Ya existe un usuario con ese nombre");
        }
        Usuario usuario = Usuario.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .nombre(request.nombre())
                .rol(Rol.CAMARERO)
                .activo(true)
                .build();
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public Usuario actualizar(Long id, ActualizarUsuarioRequest request) {
        Usuario usuario = obtener(id);
        usuario.setNombre(request.nombre());
        usuario.setActivo(request.activo());
        if (request.password() != null && !request.password().isBlank()) {
            usuario.setPassword(passwordEncoder.encode(request.password()));
        }
        return usuarioRepository.save(usuario);
    }

    @Transactional
    public void eliminar(Long id) {
        Usuario usuario = obtener(id);
        if (usuario.getRol() == Rol.ADMIN) {
            throw ApiException.badRequest("No se puede eliminar un usuario administrador");
        }
        usuarioRepository.delete(usuario);
    }
}