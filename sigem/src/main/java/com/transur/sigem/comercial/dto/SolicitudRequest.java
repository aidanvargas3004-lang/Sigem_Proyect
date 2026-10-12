package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class SolicitudRequest {

    @NotNull(message = "El ID del cliente es obligatorio")
    private Long clienteId;

    @NotBlank(message = "El tipo de solicitud es obligatorio")
    // Valores: VENTA, ALQUILER, SERVICIO
    private String tipoSolicitud;

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;
}