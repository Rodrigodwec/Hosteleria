package com.hosteleriapractica.backendpractica.dto;

import java.math.BigDecimal;

import com.hosteleriapractica.backendpractica.model.CategoriaProducto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ProductoRequest(
		@NotBlank String nombre,
		String descripcion,
		@NotNull @PositiveOrZero BigDecimal precio,
		@NotNull CategoriaProducto categoria,
		boolean disponible
) {
}
