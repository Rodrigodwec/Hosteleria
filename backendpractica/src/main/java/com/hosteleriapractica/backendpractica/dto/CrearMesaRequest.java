package com.hosteleriapractica.backendpractica.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public record CrearMesaRequest(
        @NotNull @Positive Integer numero,
        @Positive Integer capacidad
) {
}