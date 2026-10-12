package com.transur.sigem.operaciones.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** DTO de entrada para reportar una incidencia: evita la asignación masiva sobre la entidad. */
public record IncidenciaRequest(
        @NotNull Long servicioId,
        Long maquinariaId,
        @NotBlank @Pattern(regexp = "MECANICA|ACCIDENTE|CLIMA|TRAFICO|SEGURIDAD|OTRO") String tipo,
        @NotBlank @Size(max = 2000) String descripcion) {
}
