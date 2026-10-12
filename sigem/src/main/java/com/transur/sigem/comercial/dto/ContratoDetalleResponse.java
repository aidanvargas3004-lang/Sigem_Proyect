package com.transur.sigem.comercial.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ContratoDetalleResponse {
    private Long id;
    private String numero;
    private String clienteNombre;
    private String clienteDocumento;
    private String tipoContrato;
    private String estado;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private BigDecimal montoTotal;
    private Long cotizacionId;
    private String cotizacionNumero;
    private String cotizacionEstado;
    private BigDecimal cotizacionSubtotal;
    private BigDecimal cotizacionImpuestos;
    private BigDecimal cotizacionTotal;
    private String solicitudDescripcion;
    private List<ContratoLineaResponse> lineas;
}
