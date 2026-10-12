package com.transur.sigem.compras.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class OrdenCompraDetalleDTO {
    private Long repuestoId; // Nullable si es item no catalogado

    @NotNull(message = "La descripción es obligatoria")
    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad;

    @NotNull(message = "El precio unitario es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precioUnitario;
}