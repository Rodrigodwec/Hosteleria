package com.hosteleriapractica.backendpractica.service;

import com.hosteleriapractica.backendpractica.model.*;
import com.hosteleriapractica.backendpractica.repository.ComandaRepository;
import com.hosteleriapractica.backendpractica.repository.ProductoRepository;
import com.hosteleriapractica.backendpractica.security.UserPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class ComandaService {

    private final ComandaRepository comandaRepository;
    private final ProductoRepository productoRepository;

    public ComandaService(ComandaRepository comandaRepository, ProductoRepository productoRepository) {
        this.comandaRepository = comandaRepository;
        this.productoRepository = productoRepository;
    }

    @Transactional
    public Comanda agregarLinea(Long comandaId, Long productoId, int cantidad, UserPrincipal principal) {
        Comanda comanda = comandaRepository.findById(comandaId)
                .orElseThrow(() -> new IllegalStateException("Comanda no encontrada"));

        verificarPermiso(comanda, principal);

        Producto producto = productoRepository.findById(productoId)
                .orElseThrow(() -> new IllegalStateException("Producto no encontrado"));

        Optional<LineaComanda> existente = comanda.getLineas().stream()
                .filter(l -> l.getProducto().getId().equals(productoId))
                .findFirst();

        if (existente.isPresent()) {
            LineaComanda linea = existente.get();
            linea.setCantidad(linea.getCantidad() + cantidad);
        } else {
            LineaComanda linea = LineaComanda.builder()
                    .comanda(comanda)
                    .producto(producto)
                    .cantidad(cantidad)
                    .precioUnitario(producto.getPrecio())
                    .build();
            comanda.getLineas().add(linea);
        }

        return comandaRepository.save(comanda);
    }

    private void verificarPermiso(Comanda comanda, UserPrincipal principal) {
        boolean esAdmin = principal.getUsuario().getRol() == Rol.ADMIN;
        boolean esPropietario = comanda.getCamarero().getId().equals(principal.getId());
        if (!esAdmin && !esPropietario) {
            throw new IllegalStateException("Solo el camarero asignado a esta mesa puede modificar la comanda");
        }
    }
}