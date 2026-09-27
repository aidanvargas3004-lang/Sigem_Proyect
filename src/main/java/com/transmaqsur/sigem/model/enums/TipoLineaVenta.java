package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum TipoLineaVenta {
    ALQUILER("Alquiler"),
    SERVICIO("Servicio de transporte"),
    REPUESTO("Repuesto"),
    OTRO("Otro concepto");

    private final String label;
}
