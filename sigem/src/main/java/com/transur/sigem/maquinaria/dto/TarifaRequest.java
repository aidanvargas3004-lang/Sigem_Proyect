package com.transur.sigem.maquinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class TarifaRequest {
    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    @NotBlank(message = "El tipo de tarifa es obligatorio (HORA, DIA, SEMANA, MES)")
    private String tipoTarifa;

    @NotNull(message = "El precio es obligatorio")
    @Positive(message = "El precio debe ser mayor a 0")
    private BigDecimal precio;

    // ✅ CAMPOS FALTANTES AGREGADOS AQUÍ
    @NotNull(message = "La fecha de inicio de vigencia es obligatoria")
    private LocalDate vigenciaInicio;

    private LocalDate vigenciaFin; // Opcional: puede ser tarifa indefinida
}