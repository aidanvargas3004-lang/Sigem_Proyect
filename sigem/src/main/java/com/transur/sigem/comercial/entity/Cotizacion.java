package com.transur.sigem.comercial.entity;

import com.transur.sigem.cliente.entity.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "cotizaciones")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Cotizacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // RELACIÓN 1:1 CON SOLICITUD (única cotización inicial por solicitud)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "solicitud_id", nullable = false, unique = true)
    private Solicitud solicitud;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    @Column(nullable = false, unique = true, length = 30)
    private String numero; // COT-2026-001

    @Column(name = "fecha_emision", nullable = false)
    private LocalDate fechaEmision = LocalDate.now();

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(precision = 12, scale = 2)
    private BigDecimal impuestos;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    private LocalDate vigencia; // Fecha límite de validez de la cotización

    @Column(nullable = false, length = 30)
    private String estado = "BORRADOR";
    // BORRADOR, ENVIADA, APROBADA, RECHAZADA, EXPIRADA

    // RELACIÓN: Una cotización tiene múltiples líneas de detalle
    @OneToMany(mappedBy = "cotizacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CotizacionDetalle> detalles = new ArrayList<>();
}