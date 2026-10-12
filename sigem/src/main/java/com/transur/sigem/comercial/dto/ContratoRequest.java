package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ContratoRequest {
    @NotNull(message = "El ID de la cotización aprobada es obligatorio")
    private Long cotizacionId;

    private String numero;

    @NotNull(message = "El tipo de contrato es obligatorio")
    private String tipoContrato;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    private LocalDate fechaFin;
}
