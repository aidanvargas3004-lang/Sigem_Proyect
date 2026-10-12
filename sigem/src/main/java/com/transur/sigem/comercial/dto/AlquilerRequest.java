package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AlquilerRequest {

    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;

    private LocalDateTime fechaInicioReal;

    private LocalDateTime fechaFinReal;

    private String observaciones;

    @NotNull(message = "Debe tener al menos un detalle de alquiler")
    private List<AlquilerDetalleDTO> detalles;
}