package com.transur.sigem.comercial.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class AlquilerDetalleDTO {

    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    @NotNull(message = "El ID de la tarifa aplicada es obligatorio")
    private Long tarifaId;

    @NotNull(message = "Las unidades de tiempo son obligatorias")
    @Positive(message = "Las unidades deben ser mayores a 0")
    private Integer unidadesTiempo;
}