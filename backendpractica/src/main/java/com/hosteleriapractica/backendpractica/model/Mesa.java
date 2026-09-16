package com.hosteleriapractica.backendpractica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "mesas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Mesa {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private int numero;
    
    @Column(nullable = true)
    private Integer capacidad; //. int es un tipo primitivo de Java — nunca, bajo ninguna circunstancia, puede valer null; como mucho vale 0. Uso Integer que si permite nulo

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoMesa estado = EstadoMesa.LIBRE; //Para que empiece con valor Libre
    
    @ManyToOne(fetch = FetchType.LAZY) //Muchas mesas pueden apuntar al mismo camarero, por eso es "muchos a uno" visto desde Mesa. @JoinColumn(name = "camarero_id") es la columna FK que se creará en la tabla mesas
    @JoinColumn(name = "camarero_id")
    private Usuario camarero;

}
