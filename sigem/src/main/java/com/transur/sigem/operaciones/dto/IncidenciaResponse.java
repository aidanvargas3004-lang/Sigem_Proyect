package com.transur.sigem.operaciones.dto;

import com.transur.sigem.operaciones.entity.Incidencia;

import java.time.LocalDateTime;

public record IncidenciaResponse(Long id, Long servicioId, Long maquinariaId, String unidad, String tipo,
                                 String descripcion, LocalDateTime fecha, String estado, String fotoEvidenciaUrl) {

    public static IncidenciaResponse de(Incidencia i) {
        return new IncidenciaResponse(i.getId(), i.getServicio().getId(),
                i.getMaquinaria() != null ? i.getMaquinaria().getId() : null,
                i.getMaquinaria() != null ? i.getMaquinaria().getCodigoInterno() : null,
                i.getTipo(), i.getDescripcion(), i.getFecha(), i.getEstado(), i.getFotoEvidenciaUrl());
    }
}
