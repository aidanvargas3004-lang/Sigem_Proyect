package com.transur.sigem.rrhh.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentoResponse {
    private Long id;
    private String empleadoNombre;
    private String tipoDocumento;
    private String numeroDocumento;
    private LocalDate fechaEmision;
    private LocalDate fechaVencimiento;
    private String rutaArchivo;
    private Boolean estado;
    private Boolean proximoAVencer; // Flag calculado para UI
}