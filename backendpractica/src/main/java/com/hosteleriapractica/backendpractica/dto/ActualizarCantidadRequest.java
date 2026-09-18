package com.hosteleriapractica.backendpractica.dto;

import jakarta.validation.constraints.Positive;

public record ActualizarCantidadRequest(
        @Positive int cantidad
) {
}