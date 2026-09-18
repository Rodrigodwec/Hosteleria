package com.hosteleriapractica.backendpractica.controller;

import com.hosteleriapractica.backendpractica.dto.ActualizarUsuarioRequest;
import com.hosteleriapractica.backendpractica.dto.CrearUsuarioRequest;
import com.hosteleriapractica.backendpractica.dto.UsuarioDto;
import com.hosteleriapractica.backendpractica.security.UserPrincipal;
import com.hosteleriapractica.backendpractica.service.UsuarioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping("/me")
    public UsuarioDto me(@AuthenticationPrincipal UserPrincipal principal) {
        return UsuarioDto.from(principal.getUsuario());
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UsuarioDto> listarCamareros() {
        return usuarioService.listarCamareros().stream().map(UsuarioDto::from).toList();
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public UsuarioDto crear(@Valid @RequestBody CrearUsuarioRequest request) {
        return UsuarioDto.from(usuarioService.crearCamarero(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UsuarioDto actualizar(@PathVariable Long id, @Valid @RequestBody ActualizarUsuarioRequest request) {
        return UsuarioDto.from(usuarioService.actualizar(id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminar(@PathVariable Long id) {
        usuarioService.eliminar(id);
    }
}