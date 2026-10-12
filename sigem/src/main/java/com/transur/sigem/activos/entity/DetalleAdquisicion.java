package com.transur.sigem.activos.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_adquisicion")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DetalleAdquisicion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "adquisicion_id", nullable = false)
    private AdquisicionMaquinaria adquisicion;

    // RELACIÓN 1:1 CON LA UNIDAD FÍSICA REGISTRADA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false, unique = true)
    private Maquinaria maquinaria;

    @Column(name = "precio_unitario", nullable = false, precision = 15, scale = 2)
    private BigDecimal precioUnitario; // Costo específico de esta unidad dentro del lote

    @Column(columnDefinition = "TEXT")
    private String observaciones; // Estado al recibir, accesorios incluidos, etc.
}