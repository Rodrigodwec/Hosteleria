package com.hosteleriapractica.backendpractica.repository;


import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hosteleriapractica.backendpractica.model.Comanda;
import com.hosteleriapractica.backendpractica.model.EstadoComanda;

public interface ComandaRepository extends JpaRepository<Comanda, Long>{
	    Optional <Comanda> findByMesaIdAndEstado(Long mesaId, EstadoComanda estado);

}
