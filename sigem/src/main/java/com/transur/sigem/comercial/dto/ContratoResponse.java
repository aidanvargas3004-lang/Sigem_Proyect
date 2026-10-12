package com.transur.sigem.comercial.dto;

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
public class ContratoResponse {
    private Long id;
    private String numero;
    private Long cotizacionId;
    private String cotizacionNumero;
    private String clienteNombre;
    private String tipoContrato;
    private BigDecimal montoTotal;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private String estado;
}