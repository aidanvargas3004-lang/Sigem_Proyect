package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrigenAsignacion {
    ALQUILER("Alquiler"),
    SERVICIO("Servicio de transporte"),
    INTERNA("Operación interna");

    private final String label;
}
