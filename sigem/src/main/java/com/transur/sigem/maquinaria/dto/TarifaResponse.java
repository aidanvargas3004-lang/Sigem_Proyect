package com.transur.sigem.maquinaria.dto;

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
public class TarifaResponse {
    private Long id;
    private String maquinariaCodigo;
    private String tipoTarifa;
    private BigDecimal precio;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFin;
}