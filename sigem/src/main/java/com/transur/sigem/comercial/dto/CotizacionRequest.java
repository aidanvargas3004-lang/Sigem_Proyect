package com.transur.sigem.comercial.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class CotizacionRequest {
    @NotNull(message = "La solicitud es obligatoria")
    private Long solicitudId;

    private String numero;
    private LocalDate fechaEmision;
    private LocalDate vigencia;

    @NotEmpty(message = "Debe agregar al menos un detalle")
    @Valid
    private List<CotizacionDetalleDTO> detalles = new ArrayList<>();

    private String observaciones;
}
