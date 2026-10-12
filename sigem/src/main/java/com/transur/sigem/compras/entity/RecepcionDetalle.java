package com.transur.sigem.compras.entity;

import com.transur.sigem.logistica.entity.Repuesto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "recepcion_detalles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RecepcionDetalle {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recepcion_id", nullable = false)
    private RecepcionCompra recepcion;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repuesto_id")
    private Repuesto repuesto;

    @Column(length = 255)
    private String descripcion;

    @Column(name = "cantidad_recibida", nullable = false)
    private Integer cantidadRecibida; // Siempre > 0 (CHECK en BD)

    @Column(columnDefinition = "TEXT")
    private String observacion; // Estado físico, daños, diferencias, etc.
}