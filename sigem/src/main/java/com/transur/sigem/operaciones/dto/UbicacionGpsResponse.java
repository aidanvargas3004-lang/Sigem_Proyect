package com.transur.sigem.operaciones.dto;

import com.transur.sigem.operaciones.entity.UbicacionGps;

import java.time.LocalDateTime;

/** DTO de salida de una posición GPS (no expone la entidad JPA). */
public record UbicacionGpsResponse(Long id, Long maquinariaId, String unidad, Long servicioId, Double latitud,
                                   Double longitud, Double velocidad, String referencia, LocalDateTime fechaHora,
                                   String registradoPor) {

    public static UbicacionGpsResponse de(UbicacionGps g) {
        return new UbicacionGpsResponse(g.getId(), g.getMaquinaria().getId(), g.getMaquinaria().getCodigoInterno(),
                g.getServicio() != null ? g.getServicio().getId() : null, g.getLatitud(), g.getLongitud(),
                g.getVelocidad(), g.getReferencia(), g.getFechaHora(), g.getRegistradoPor());
    }
}
