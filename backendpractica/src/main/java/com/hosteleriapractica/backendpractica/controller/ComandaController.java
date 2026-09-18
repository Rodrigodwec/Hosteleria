package com.hosteleriapractica.backendpractica.controller;

import com.hosteleriapractica.backendpractica.dto.ActualizarCantidadRequest;
import com.hosteleriapractica.backendpractica.dto.AgregarLineaRequest;
import com.hosteleriapractica.backendpractica.dto.ComandaDto;
import com.hosteleriapractica.backendpractica.security.UserPrincipal;
import com.hosteleriapractica.backendpractica.service.ComandaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/comandas")
public class ComandaController {

    private final ComandaService comandaService;

    public ComandaController(ComandaService comandaService) {
        this.comandaService = comandaService;
    }

    @PostMapping("/{id}/lineas")
    public ComandaDto agregarLinea(@PathVariable Long id,
                                    @Valid @RequestBody AgregarLineaRequest request,
                                    @AuthenticationPrincipal UserPrincipal principal) {
        return ComandaDto.from(comandaService.agregarLinea(id, request.productoId(), request.cantidad(), principal));
    }

    @PutMapping("/{id}/lineas/{lineaId}")
    public ComandaDto actualizarCantidad(@PathVariable Long id, @PathVariable Long lineaId,
                                          @Valid @RequestBody ActualizarCantidadRequest request,
                                          @AuthenticationPrincipal UserPrincipal principal) {
        return ComandaDto.from(comandaService.actualizarCantidad(id, lineaId, request.cantidad(), principal));
    }

    @DeleteMapping("/{id}/lineas/{lineaId}")
    public ComandaDto eliminarLinea(@PathVariable Long id, @PathVariable Long lineaId,
                                     @AuthenticationPrincipal UserPrincipal principal) {
        return ComandaDto.from(comandaService.eliminarLinea(id, lineaId, principal));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void eliminarComanda(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        comandaService.eliminarComanda(id, principal);
    }

    @PostMapping("/{id}/cobrar")
    public ComandaDto cobrar(@PathVariable Long id, @AuthenticationPrincipal UserPrincipal principal) {
        return ComandaDto.from(comandaService.cobrar(id, principal));
    }
}