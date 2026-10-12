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
public class EmpleadoResponse {
    private Long id;
    private String documento;
    private String nombres;
    private String apellidos;
    private String cargo;
    private String telefono;
    private String email;
    private LocalDate fechaIngreso;
    private Boolean estado;
    private String username; // Nombre de usuario vinculado (si existe)
    private int cantidadDocumentos; // Resumen rápido
}