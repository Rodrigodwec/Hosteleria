package com.hosteleriapractica.backendpractica.service;

import com.hosteleriapractica.backendpractica.dto.CrearMesaRequest;
import com.hosteleriapractica.backendpractica.exception.ApiException;
import com.hosteleriapractica.backendpractica.model.Comanda;
import com.hosteleriapractica.backendpractica.model.EstadoMesa;
import com.hosteleriapractica.backendpractica.model.Mesa;
import com.hosteleriapractica.backendpractica.model.Usuario;
import com.hosteleriapractica.backendpractica.repository.ComandaRepository;
import com.hosteleriapractica.backendpractica.repository.MesaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;
    private final ComandaRepository comandaRepository;
    
    public MesaService(MesaRepository mesaRepository, ComandaRepository comandaRepository) {
        this.mesaRepository = mesaRepository;
        this.comandaRepository = comandaRepository;
    }
    

    public List<Mesa> listarTodas() {
        return mesaRepository.findAll();
    }
    
    public Mesa crear(CrearMesaRequest request) {
        Mesa mesa = Mesa.builder()
                .numero(request.numero())
                .capacidad(request.capacidad())
                .build();
        return mesaRepository.save(mesa);
    }
    
    public Comanda ocupar(Long mesaId, Usuario camarero) {
        Mesa mesa = mesaRepository.findById(mesaId)
                .orElseThrow(() -> ApiException.notFound("Mesa no encontrada"));

        if (mesa.getEstado() == EstadoMesa.OCUPADA) {
            throw ApiException.conflict("La mesa ya está ocupada");
        }
        
        Comanda comanda = Comanda.builder()
        		.mesa(mesa)
        		.camarero(camarero)
        		.build();
        comanda = comandaRepository.save(comanda);

        mesa.setEstado(EstadoMesa.OCUPADA);
        mesa.setCamarero(camarero);
        mesaRepository.save(mesa);

        return comanda;
    }
}