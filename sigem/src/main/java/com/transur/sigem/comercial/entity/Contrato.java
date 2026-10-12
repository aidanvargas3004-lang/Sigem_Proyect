package com.transur.sigem.comercial.entity;

import com.transur.sigem.cliente.entity.Cliente;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "contratos")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Contrato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id", nullable = false)
    private Cliente cliente;

    // RELACIÓN 1:1 ÚNICA CON COTIZACIÓN APROBADA
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cotizacion_id", nullable = false, unique = true)
    private Cotizacion cotizacion;

    @Column(nullable = false, unique = true, length = 30)
    private String numero; // CTR-2026-001

    // TIPO DE CONTRATO: Determina el flujo posterior (Venta/Alquiler/Servicio)
    @Column(name = "tipo_contrato", nullable = false, length = 30)
    private String tipoContrato;
    // VENTA, ALQUILER, SERVICIO

    @Column(name = "fecha_inicio", nullable = false)
    private LocalDate fechaInicio;

    private LocalDate fechaFin; // Null para contratos de venta (instantáneos)

    @Column(name = "monto_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal montoTotal;

    @Column(nullable = false, length = 30)
    private String estado = "VIGENTE";
    // VIGENTE, SUSPENDIDO, FINALIZADO, CANCELADO
}