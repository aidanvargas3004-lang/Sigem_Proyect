package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoIncidencia {
    REPORTADA("Reportada"),
    EN_ATENCION("En atención"),
    RESUELTA("Resuelta");

    private final String label;
}
