package com.transur.sigem.maquinaria.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class TipoMaquinariaRequest {
    @NotBlank(message = "El nombre del tipo es obligatorio")
    private String nombre; // EJ: EXCAVADORA, VOLQUETE

    private String descripcion;

    private String imageUrl;
}