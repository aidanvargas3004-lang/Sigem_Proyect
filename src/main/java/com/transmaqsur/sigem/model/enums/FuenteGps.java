package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FuenteGps {
    MANUAL("Registro manual"),
    API("Dispositivo GPS (API)"),
    SIMULADOR("Simulador");

    private final String label;
}
