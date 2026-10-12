package com.transur.sigem.compras.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class RecepcionDetalleDTO {
    private Long repuestoId;
    private String descripcion;

    @NotNull(message = "La cantidad recibida es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidadRecibida;

    private String observacion;
}