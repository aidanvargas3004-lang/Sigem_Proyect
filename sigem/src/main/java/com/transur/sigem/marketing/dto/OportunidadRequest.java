package com.transur.sigem.marketing.dto;

import jakarta.validation.constraints.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class OportunidadRequest {
    private Long leadId;      // Opcional
   // Opcional


    private String descripcion;

    @PositiveOrZero(message = "El monto estimado debe ser >= 0")
    private BigDecimal montoEstimado;

    @Min(value = 0, message = "La probabilidad mínima es 0")
    @Max(value = 100, message = "La probabilidad máxima es 100")
    private Integer probabilidad;


    @NotNull
    private Long clienteId; // ID del cliente seleccionado
    @NotBlank
    private String estado;

    private LocalDate fechaCierreEstimado;
}