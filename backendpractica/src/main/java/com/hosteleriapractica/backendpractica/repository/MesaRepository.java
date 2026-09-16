package com.hosteleriapractica.backendpractica.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.hosteleriapractica.backendpractica.model.EstadoMesa;
import com.hosteleriapractica.backendpractica.model.Mesa;

public interface MesaRepository extends JpaRepository<Mesa, Long> {
	    Optional<Mesa> findByNumero(Integer numero);
	    boolean existsByNumero(Integer numero);
	    List<Mesa> findByCamareroId(Long camareroId);//Devuelve una lista con las mesas de ese camarero
	    List<Mesa> findByEstado(EstadoMesa estado); //estado es del tipo EstadoMesa
}
