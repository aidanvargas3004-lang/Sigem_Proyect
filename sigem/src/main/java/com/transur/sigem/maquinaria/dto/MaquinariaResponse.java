package com.transur.sigem.maquinaria.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MaquinariaResponse {
    private Long id;
    private String codigoInterno;
    private String marca;
    private String modelo;
    private Integer anio;      // ✅ Debe llamarse 'anio', no 'anioFabricacion'
    private String tipoNombre;
    private String placa;
    private BigDecimal horometroActual;
    private String estadoOperativo;
    private String destinoOperativo;
    private BigDecimal precioBaseAlquiler;
    private BigDecimal precioBaseVenta;
    private String imageUrl;
}