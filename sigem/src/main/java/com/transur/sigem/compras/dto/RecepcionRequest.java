package com.transur.sigem.compras.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class RecepcionRequest {
    @NotNull(message = "El ID de la orden de compra es obligatorio")
    private Long ordenCompraId;

    @NotBlank(message = "El número de recepción/guía es obligatorio")
    private String numero;

    private LocalDate fechaRecepcion;
    private Long usuarioId; // Quien recibe
    private String observaciones;

    @NotNull(message = "Debe tener al menos un detalle")
    private List<RecepcionDetalleDTO> detalles;
}