package com.transur.sigem.activos.dto;

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
public class AdquisicionResponse {
    private Long id;
    private String proveedorNombre;
    private String tipoAdquisicion;
    private BigDecimal costoTotal;
    private LocalDate fechaAdquisicion;
    private String destinoInicial;
    private String estado;
    private int cantidadUnidades; // Resumen rápido
}