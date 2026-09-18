package com.hosteleriapractica.backendpractica.dto;

import java.math.BigDecimal;

import com.hosteleriapractica.backendpractica.model.Producto;

public record ProductoDto(
        Long id,
        String nombre,
        String descripcion,
        BigDecimal precio,
        String categoria,
        Boolean disponible
		) {

    public static ProductoDto from(Producto producto) {
        return new ProductoDto(
                producto.getId(),
                producto.getNombre(),
                producto.getDescripcion(),
                producto.getPrecio(),
                producto.getCategoria().name(),
                producto.isDisponible()
        );
    }
}