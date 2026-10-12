package com.transur.sigem.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;

@Data
public class InventarioMaquinariaRequest {
    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    private Long ubicacionId;

    @NotBlank(message = "La situación logística es obligatoria")
    private String situacionLogistica;

    private LocalDate fechaIngreso;
    private String observaciones;
}