package com.transur.sigem.operaciones.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDateTime;

@Data
public class AsignacionRequest {

    @NotNull(message = "El ID del servicio es obligatorio")
    private Long servicioId;

    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    private Long empleadoId; // Opcional según tu entidad

    private LocalDateTime fechaAsignacion;
    private String estado = "ACTIVA";
}