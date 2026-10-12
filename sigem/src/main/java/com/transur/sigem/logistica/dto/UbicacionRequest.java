package com.transur.sigem.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UbicacionRequest {
    @NotNull(message = "El ID del almacén es obligatorio")
    private Long almacenId;

    @NotBlank(message = "El código de ubicación es obligatorio")
    private String codigo;

    private String nombre;
    private String descripcion;
}