package com.hosteleriapractica.backendpractica.repository;

import com.hosteleriapractica.backendpractica.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> { //JpaRepository<Usuario, Long> te da además, gratis, sin escribir nada: save(), findById(), findAll(), deleteById()... el CRUD básico ya viene incluido solo con extender esa interfaz.
    Optional<Usuario> findByUsername(String username);
    boolean existsByUsername(String username);
}