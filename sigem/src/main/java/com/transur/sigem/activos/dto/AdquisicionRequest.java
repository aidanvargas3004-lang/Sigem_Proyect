package com.transur.sigem.activos.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class AdquisicionRequest {

    @NotNull(message = "El ID del proveedor es obligatorio")
    private Long proveedorId;

    @NotBlank(message = "El tipo de adquisición es obligatorio")
    private String tipoAdquisicion; // COMPRA_DIRECTA, LEASING, etc.

    @NotNull(message = "La fecha de adquisición es obligatoria")
    private LocalDate fechaAdquisicion;

    @NotBlank(message = "El destino inicial es obligatorio")
    private String destinoInicial; // VENTA, ALQUILER, USO_INTERNO

    private String numeroSerieFabrica;
    private String documentosLegalUrl;

    @NotNull(message = "Debe tener al menos un detalle")
    private List<DetalleAdquisicionDTO> detalles;
}