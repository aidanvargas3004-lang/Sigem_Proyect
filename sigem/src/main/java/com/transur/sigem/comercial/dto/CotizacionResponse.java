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
public class CotizacionResponse {
    private Long id;
    private String numero;
    private Long solicitudId;
    private String clienteNombre;
    private String tipoSolicitud; // Viene de la solicitud vinculada
    private BigDecimal total;
    private LocalDate fechaEmision;
    private LocalDate vigencia;
    private String estado;
}