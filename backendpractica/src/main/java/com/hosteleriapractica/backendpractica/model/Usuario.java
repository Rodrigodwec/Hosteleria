package com.hosteleriapractica.backendpractica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usuarios") //@Entity mas esto = "esto es una tabla llamada usuarios"
@Data //(Lombok)genera getters/setters/toString/equals automáticamente
@NoArgsConstructor
@AllArgsConstructor// mas el anterior NoArgs dos constructores (vacío, y con todos los campos) — JPA exige que exista un constructor vacío, aunque tú nunca lo llames a mano
@Builder //te permite construir objetos así: Usuario.builder().username("admin").nombre("Admin").build() (lo usarás mucho al crear datos de prueba
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) //clave primaria autoincremental, la genera MySQL
    private Long id;

    @Column(nullable = false, unique = true, length = 50) //restricciones a nivel de columna SQL (NOT NULL, UNIQUE)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, length = 100)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Rol rol;

    @Column(nullable = false)
    @Builder.Default //sobre activo → sin esto, si usas el builder y no especificas activo, el valor por defecto (true) se perdería y quedaría en false
    private boolean activo = true;
}