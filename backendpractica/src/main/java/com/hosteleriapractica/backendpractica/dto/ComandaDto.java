package com.hosteleriapractica.backendpractica.dto;

import com.hosteleriapractica.backendpractica.model.Comanda;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ComandaDto(
        Long id,
        Long mesaId,
        Integer mesaNumero,
        Long camareroId,
        String camareroNombre,
        String estado,
        LocalDateTime fechaApertura,
        List<LineaComandaDto> lineas,
        BigDecimal total
) {
    public static ComandaDto from(Comanda comanda) {
        List<LineaComandaDto> lineas = comanda.getLineas().stream()
                .map(LineaComandaDto::from)
                .toList();

        BigDecimal total = lineas.stream()
                .map(LineaComandaDto::subtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return new ComandaDto(
                comanda.getId(),
                comanda.getMesa().getId(),
                comanda.getMesa().getNumero(),
                comanda.getCamarero().getId(),
                comanda.getCamarero().getNombre(),
                comanda.getEstado().name(),
                comanda.getFechaApertura(),
                lineas,
                total
        );
    }
}