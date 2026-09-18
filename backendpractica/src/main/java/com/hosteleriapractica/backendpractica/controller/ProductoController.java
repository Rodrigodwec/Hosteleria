package com.hosteleriapractica.backendpractica.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;


import com.hosteleriapractica.backendpractica.dto.ProductoDto;
import com.hosteleriapractica.backendpractica.dto.ProductoRequest;
import com.hosteleriapractica.backendpractica.service.ProductoService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/productos")
public class ProductoController {

    private final ProductoService productoService;

    public ProductoController(ProductoService productoService) {
        this.productoService = productoService;
    }
    
    @GetMapping
    public List<ProductoDto> listar() {
    	return productoService.listarTodos().stream().map(ProductoDto::from).toList();
    }
    
    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ProductoDto crear(@Valid @RequestBody ProductoRequest request) {
    	return ProductoDto.from(productoService.crear(request));
    }
}
