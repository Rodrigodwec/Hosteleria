package com.hosteleriapractica.backendpractica.controller;

import com.hosteleriapractica.backendpractica.dto.CrearMesaRequest;
import com.hosteleriapractica.backendpractica.dto.MesaDto;
import com.hosteleriapractica.backendpractica.service.MesaService;

import jakarta.validation.Valid;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/mesas")
public class MesaController {

    private final MesaService mesaService;

    public MesaController(MesaService mesaService) {
        this.mesaService = mesaService;
    }

    @GetMapping
    public List<MesaDto> listar() {
        return mesaService.listarTodas().stream().map(MesaDto::from).toList(); //"coge la lista, conviértela en un flujo (stream), a cada elemento aplícale la función MesaDto.from(...) (map), y vuelve a juntarlo todo en una lista (toList)"
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public MesaDto crear(@Valid @RequestBody CrearMesaRequest request) {
        return MesaDto.from(mesaService.crear(request));
    }
}