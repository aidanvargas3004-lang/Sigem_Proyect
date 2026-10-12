package com.transur.sigem.comercial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReporteVentasDTO {
    private LocalDate desde;
    private LocalDate hasta;
    private String tipoContrato;
    private String estado;

    private long totalSolicitudes;
    private long solicitudesPendientes;
    private long totalCotizaciones;
    private long cotizacionesAprobadas;
    private long totalContratos;
    private long contratosVigentes;
    private long contratosFinalizados;
    private BigDecimal montoContratado;
    private BigDecimal montoVigente;
    private BigDecimal montoFinalizado;

    private long contratosVenta;
    private long contratosAlquiler;
    private long contratosServicio;
    private BigDecimal montoVenta;
    private BigDecimal montoAlquiler;
    private BigDecimal montoServicio;

    private List<ContratoResponse> contratos;
}
