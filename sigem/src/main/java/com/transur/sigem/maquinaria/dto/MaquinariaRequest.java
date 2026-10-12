package com.transur.sigem.maquinaria.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class MaquinariaRequest {

    @NotBlank(message = "El código interno es obligatorio")
    private String codigoInterno;

    @NotNull(message = "El ID del tipo es obligatorio")
    private Long tipoId; // Este ID se usará para buscar la entidad TipoMaquinaria

    @NotBlank(message = "La marca es obligatoria")
    private String marca;

    @NotBlank(message = "El modelo es obligatorio")
    private String modelo;

    @NotNull(message = "El año es obligatorio")
    @Positive(message = "El año debe ser positivo")
    private Integer anio; // Coincide EXACTO con tu entidad 'private Integer anio'

    private String placa;

    private BigDecimal horometroActual;
    private BigDecimal kilometrajeActual;
    private java.time.LocalDate soatVencimiento;
    private java.time.LocalDate revisionTecnicaVencimiento;

    private String estadoOperativo;

    private String destinoOperativo;

    private BigDecimal precioBaseAlquiler;

    private BigDecimal precioBaseVenta;

    private String imageUrl;
}