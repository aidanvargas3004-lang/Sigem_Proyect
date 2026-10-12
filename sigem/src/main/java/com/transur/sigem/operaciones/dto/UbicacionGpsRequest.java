package com.transur.sigem.operaciones.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** DTO de entrada para registrar una posición GPS (formulario o API). */
@Data
public class UbicacionGpsRequest {

    @NotNull
    private Long maquinariaId;

    private Long servicioId;

    @NotNull
    @DecimalMin("-90")
    @DecimalMax("90")
    private Double latitud;

    @NotNull
    @DecimalMin("-180")
    @DecimalMax("180")
    private Double longitud;

    private Double velocidad;

    @Size(max = 200)
    private String referencia;
}
