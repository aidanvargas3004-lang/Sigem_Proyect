package com.transur.sigem.comercial.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Entity
@Table(name = "ventas")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN 1:1 ÚNICA CON CONTRATO DE VENTA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "contrato_id", nullable = false, unique = true)
    private Contrato contrato;

    @Column(nullable = false, unique = true, length = 30)
    private String numero; // VTA-2026-001

    @Column(name = "fecha_venta", nullable = false)
    private LocalDate fechaVenta = LocalDate.now();

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal impuestos;

    @Column(name = "total_final", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalFinal;

    @Column(nullable = false, length = 30)
    private String estado = "COMPLETADA";
    // COMPLETADA, ENTREGADA, GARANTIA_ACTIVA

    // RELACIÓN: Una venta incluye múltiples máquinas transferidas
    @OneToMany(mappedBy = "venta", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<VentaDetalle> detalles;
}