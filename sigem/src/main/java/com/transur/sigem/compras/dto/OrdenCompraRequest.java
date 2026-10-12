package com.transur.sigem.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class OrdenCompraRequest {
    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    @NotBlank(message = "El número de OC es obligatorio")
    private String numero;

    private LocalDate fechaEmision;

    private Long usuarioId; // Quien genera la orden

    @NotNull(message = "Debe tener al menos un detalle")
    private List<OrdenCompraDetalleDTO> detalles;
}