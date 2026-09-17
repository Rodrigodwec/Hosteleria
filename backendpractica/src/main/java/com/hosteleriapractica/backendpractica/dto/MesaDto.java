package com.hosteleriapractica.backendpractica.dto;

import com.hosteleriapractica.backendpractica.model.Mesa;

public record MesaDto(
        Long id,
        Integer numero,
        Integer capacidad,
        String estado,
        Long camareroId,
        String camareroNombre
) {
    public static MesaDto from(Mesa mesa) {
        return new MesaDto(
                mesa.getId(),
                mesa.getNumero(),
                mesa.getCapacidad(),
                mesa.getEstado().name(),
                mesa.getCamarero() != null ? mesa.getCamarero().getId() : null,
                mesa.getCamarero() != null ? mesa.getCamarero().getNombre() : null
        );
    }
}