package com.hosteleriapractica.backendpractica.repository;

import com.hosteleriapractica.backendpractica.model.CategoriaProducto;
import com.hosteleriapractica.backendpractica.model.Producto;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
	List<Producto> findByCategoria(CategoriaProducto categoria);
}