package com.hosteleriapractica.backendpractica.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

public record ActualizarPosicionRequest(
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double posX,
        @NotNull @DecimalMin("0.0") @DecimalMax("100.0") Double posY
) {
}