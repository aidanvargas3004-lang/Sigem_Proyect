package com.transur.sigem.operaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ServicioRequest {

    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;

    @NotNull(message = "El ID del tipo de servicio es obligatorio")
    private Long tipoServicioId;

    private String origen;
    private String destino;

    @NotNull(message = "La fecha programada es obligatoria")
    private LocalDateTime fechaProgramada;
    @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime fechaFinProgramada;

    private String estado = "POR_ASIGNAR";
    private String observaciones;
}