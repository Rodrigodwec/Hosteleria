package com.hosteleriapractica.backendpractica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record AgregarLineaRequest(
        @NotNull Long productoId,
        @Positive int cantidad
) {
}