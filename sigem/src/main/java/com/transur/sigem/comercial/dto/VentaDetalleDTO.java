package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class VentaDetalleDTO {

    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    @NotNull(message = "El precio final de venta es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioFinal;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;
}