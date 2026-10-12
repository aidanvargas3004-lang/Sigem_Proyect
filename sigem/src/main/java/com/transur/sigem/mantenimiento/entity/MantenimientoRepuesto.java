package com.transur.sigem.mantenimiento.entity;

import com.transur.sigem.logistica.entity.Repuesto;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Entity
@Table(name = "mantenimiento_repuestos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoRepuesto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mantenimiento_id", nullable = false)
    private Mantenimiento mantenimiento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repuesto_id", nullable = false)
    private Repuesto repuesto;

    @Column(nullable = false)
    private Integer cantidad; // Siempre > 0 (CHECK en BD)

    @Column(name = "costo_unitario", precision = 12, scale = 2)
    private BigDecimal costoUnitario; // Precio al momento del uso (snapshot)
}