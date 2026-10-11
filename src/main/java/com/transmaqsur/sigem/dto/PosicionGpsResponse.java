package com.transmaqsur.sigem.dto;

import com.transmaqsur.sigem.model.UbicacionGps;

/** DTO de salida de la API GPS: expone solo los datos necesarios, nunca la entidad JPA. */
public record PosicionGpsResponse(String unidad, double latitud, double longitud, Double velocidad, String fechaHora, String estado) {

    public static PosicionGpsResponse de(UbicacionGps g) {
        return new PosicionGpsResponse(g.getUnidad().getCodigo(), g.getLatitud(), g.getLongitud(), g.getVelocidad(),
                g.getFechaHora().toString(), g.getUnidad().getEstado().getLabel());
    }
}
