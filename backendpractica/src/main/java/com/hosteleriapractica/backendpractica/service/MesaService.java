package com.hosteleriapractica.backendpractica.service;

import com.hosteleriapractica.backendpractica.model.Mesa;
import com.hosteleriapractica.backendpractica.repository.MesaRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MesaService {

    private final MesaRepository mesaRepository;

    public MesaService(MesaRepository mesaRepository) {
        this.mesaRepository = mesaRepository;
    }

    public List<Mesa> listarTodas() {
        return mesaRepository.findAll();
    }
}