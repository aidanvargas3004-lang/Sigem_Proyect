package com.transur.sigem.operaciones.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServicioResponse {
    private Long id;
    private String numeroContrato;
    private String tipoServicioNombre;
    private String origen;
    private String destino;
    private LocalDateTime fechaProgramada;
    private String estado;
    private String observaciones;
}