package com.transur.sigem.logistica.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AlmacenRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    private String descripcion;
    private String direccion;
    private Long responsableId; // ID del Empleado
}