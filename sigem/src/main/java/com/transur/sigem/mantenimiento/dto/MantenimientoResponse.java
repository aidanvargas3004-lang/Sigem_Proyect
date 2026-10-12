package com.transur.sigem.mantenimiento.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MantenimientoResponse {
    private Long id;
    private String maquinariaCodigo;
    private String tecnicoNombre;
    private String tipoMantenimiento;
    private String descripcion;
    private LocalDate fechaProgramada;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal costoTotal;
    private String estado;
    private int cantidadRepuestos;
}