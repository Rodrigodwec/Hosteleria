package com.hosteleriapractica.backendpractica.dto;

import com.hosteleriapractica.backendpractica.model.LineaComanda;

import java.math.BigDecimal;

public record LineaComandaDto(
        Long id,
        Long productoId,
        String productoNombre,
        String categoria,
        int cantidad,
        BigDecimal precioUnitario,
        BigDecimal subtotal
) {
    public static LineaComandaDto from(LineaComanda linea) {
        return new LineaComandaDto(
                linea.getId(),
                linea.getProducto().getId(),
                linea.getProducto().getNombre(),
                linea.getProducto().getCategoria().name(),
                linea.getCantidad(),
                linea.getPrecioUnitario(),
                linea.getPrecioUnitario().multiply(BigDecimal.valueOf(linea.getCantidad()))
        );
    }
}