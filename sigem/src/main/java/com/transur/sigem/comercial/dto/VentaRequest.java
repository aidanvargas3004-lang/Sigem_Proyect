package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.util.List;

@Data
public class VentaRequest {

    @NotNull(message = "El ID del contrato es obligatorio")
    private Long contratoId;

    @NotBlank(message = "El número de venta es obligatorio")
    private String numero;

    @NotNull(message = "Debe tener al menos un detalle de venta")
    private List<VentaDetalleDTO> detalles;
}