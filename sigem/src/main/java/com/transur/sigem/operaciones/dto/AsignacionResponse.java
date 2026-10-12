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
public class AsignacionResponse {
    private Long id;
    private Long servicioId;
    private String maquinariaCodigo; // Usamos código interno en vez de objeto completo
    private String empleadoNombre;   // Nombre concatenado del operario
    private LocalDateTime fechaAsignacion;
    private String estado;
}