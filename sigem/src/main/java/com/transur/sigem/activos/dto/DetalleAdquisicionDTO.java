package com.transur.sigem.activos.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class DetalleAdquisicionDTO {

    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;

    private String observaciones;
}