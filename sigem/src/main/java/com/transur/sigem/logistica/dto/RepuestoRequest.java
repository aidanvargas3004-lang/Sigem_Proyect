package com.transur.sigem.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class RepuestoRequest {
    @NotBlank(message = "El código SKU es obligatorio")
    private String codigo;

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;

    @NotNull(message = "La categoría es obligatoria")
    private Long categoriaId;

    @PositiveOrZero(message = "El stock inicial debe ser >= 0")
    private Integer stockActual = 0;

    @PositiveOrZero(message = "El stock mínimo debe ser >= 0")
    private Integer stockMinimo = 0;

    private String unidadMedida;
}