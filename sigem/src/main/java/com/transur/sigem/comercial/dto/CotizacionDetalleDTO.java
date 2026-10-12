package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CotizacionDetalleDTO {
    private Long maquinariaId;
    private Long tarifaId;

    private String descripcion;

    @NotNull(message = "La cantidad es obligatoria")
    @Positive(message = "La cantidad debe ser mayor a 0")
    private Integer cantidad = 1;

    private BigDecimal precioUnitario;
}
