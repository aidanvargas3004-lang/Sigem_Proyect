package com.transur.sigem.mantenimiento.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.time.LocalDate;
import java.util.List;

@Data
public class MantenimientoRequest {

    @NotNull(message = "El ID de la maquinaria es obligatorio")
    private Long maquinariaId;

    private Long empleadoId; // Opcional: técnico interno

    @NotBlank(message = "El tipo de mantenimiento es obligatorio")
    private String tipoMantenimiento; // PREVENTIVO, CORRECTIVO

    @NotBlank(message = "La descripción es obligatoria")
    private String descripcion;

    private LocalDate fechaProgramada;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;

    private String estado = "PROGRAMADO";
    private String observaciones;

    private List<RepuestoUsadoDTO> repuestosUsados;
}