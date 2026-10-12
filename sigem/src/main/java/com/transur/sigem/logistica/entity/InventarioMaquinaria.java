package com.transur.sigem.logistica.entity;

import com.transur.sigem.maquinaria.entity.Maquinaria;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Entity
@Table(name = "inventario_maquinaria")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class InventarioMaquinaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN 1:1 FÍSICA: Cada máquina tiene UNA ubicación física exacta
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "maquinaria_id", nullable = false, unique = true)
    private Maquinaria maquinaria;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id")
    private UbicacionAlmacen ubicacion;

    @Column(name = "situacion_logistica", nullable = false, length = 30)
    private String situacionLogistica = "EN_PATIO_CENTRAL";
    // Valores: EN_PATIO_CENTRAL, EN_OBRA_CLIENTE, EN_TALLER, EN_TRANSITO, EN_ALMACEN

    @Column(name = "fecha_ingreso", nullable = false)
    private LocalDate fechaIngreso = LocalDate.now();

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}