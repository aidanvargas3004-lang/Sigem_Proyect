package com.transur.sigem.operaciones.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tipos_servicio")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TipoServicio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 100)
    private String nombre; // TRANSPORTE_CARGA, MOVIMIENTO_TIERRA, GRUA, COMPACTACION

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    @Column(name = "imagen_url", length = 500)
    private String imageUrl;

    @Column(nullable = false)
    private Boolean estado = true;
}