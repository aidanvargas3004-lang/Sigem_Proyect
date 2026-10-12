package com.transur.sigem.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ResumenAuditoriaDTO {
    private String modulo;
    private String accion;
    private String entidad;
    private Long entidadId;
    private String nombreUsuario;
    private String fecha;
}