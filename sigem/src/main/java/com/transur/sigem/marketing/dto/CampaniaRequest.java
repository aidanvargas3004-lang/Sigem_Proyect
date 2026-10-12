package com.transur.sigem.marketing.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class CampaniaRequest {

    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion; // Usamos descripción en lugar de canal

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "El presupuesto es obligatorio")
    private BigDecimal presupuesto;

    @NotBlank(message = "El estado es obligatorio")
    private String estado; // ACTIVA, FINALIZADA, PAUSADA
}