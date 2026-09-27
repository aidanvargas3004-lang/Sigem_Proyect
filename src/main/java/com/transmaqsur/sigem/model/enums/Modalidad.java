package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/** Modalidad de cobro de un alquiler, servicio o línea de cotización. */
@Getter
@RequiredArgsConstructor
public enum Modalidad {
    POR_HORA("Por hora", "h"),
    POR_DIA("Por día", "día"),
    POR_KM("Por kilómetro", "km"),
    POR_VIAJE("Por viaje", "viaje");

    private final String label;
    private final String unidad;
}
