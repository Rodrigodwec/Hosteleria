package com.hosteleriapractica.backendpractica.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comandas")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comanda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false) //fetch = FetchType.LAZY = No cargues este dato hasta que alguien lo pida.  optional=false porque una comanda siempre pertenece a una mesa y a un camarero concretos, nunca puede quedar "huérfana" desde el momento en que se crea
    @JoinColumn(name = "mesa_id", nullable = false)
    private Mesa mesa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "camarero_id", nullable = false)
    private Usuario camarero;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private EstadoComanda estado = EstadoComanda.ABIERTA;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime fechaApertura = LocalDateTime.now();

    private LocalDateTime fechaCierre;

    @OneToMany(mappedBy = "comanda", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @Builder.Default
    private List<LineaComanda> lineas = new ArrayList<>();
}