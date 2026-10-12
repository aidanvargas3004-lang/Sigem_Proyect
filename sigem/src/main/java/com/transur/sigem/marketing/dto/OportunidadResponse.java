package com.transur.sigem.marketing.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OportunidadResponse {
    private Long id;
    private String leadNombre;     // Nombres del lead si existe
    private String clienteRazonSocial;  // Razón social del cliente si existe
    private String descripcion;
    private BigDecimal montoEstimado;
    private Integer probabilidad;
    private String estado;
    private String clienteNombre;
    private LocalDateTime fechaCreacion;

}