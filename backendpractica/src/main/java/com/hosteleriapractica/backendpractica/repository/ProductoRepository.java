package com.hosteleriapractica.backendpractica.repository;

import com.hosteleriapractica.backendpractica.model.Producto;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductoRepository extends JpaRepository<Producto, Long> {
}