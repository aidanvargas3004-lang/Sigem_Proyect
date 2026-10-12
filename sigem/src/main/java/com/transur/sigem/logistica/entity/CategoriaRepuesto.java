package com.transur.sigem.logistica.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categorias_repuesto")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRepuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // EJ: FILTROS, NEUMATICOS, HIDRAULICA

    @Column(columnDefinition = "TEXT")
    private String descripcion;
}