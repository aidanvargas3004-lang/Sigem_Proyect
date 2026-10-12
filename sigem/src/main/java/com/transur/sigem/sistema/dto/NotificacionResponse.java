package com.transur.sigem.sistema.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionResponse {
    private Long id;
    private String titulo;
    private String mensaje;
    private String tipo;       // INFO, WARNING, ERROR, SUCCESS
    private Boolean leida;
    private LocalDateTime fecha;
    private String enlace;
}