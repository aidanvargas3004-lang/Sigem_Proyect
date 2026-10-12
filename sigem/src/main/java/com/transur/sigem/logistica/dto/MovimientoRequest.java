package com.transur.sigem.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class MovimientoRequest {
    @NotNull(message = "El ID del repuesto es obligatorio")
    private Long repuestoId;

    @NotBlank(message = "El tipo de movimiento es obligatorio")
    private String tipoMovimiento; // ENTRADA, SALIDA, AJUSTE, DEVOLUCION

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser positiva")
    private Integer cantidad;

    private Long ubicacionId;
    private String motivo;
    private Long usuarioId; // Quien registra
}