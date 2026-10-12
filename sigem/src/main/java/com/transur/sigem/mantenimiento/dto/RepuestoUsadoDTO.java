package com.transur.sigem.mantenimiento.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class RepuestoUsadoDTO {

    @NotNull(message = "El ID del repuesto es obligatorio")
    private Long repuestoId;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El costo unitario es obligatorio")
    @Positive(message = "El costo debe ser mayor a 0")
    private BigDecimal costoUnitario;
}