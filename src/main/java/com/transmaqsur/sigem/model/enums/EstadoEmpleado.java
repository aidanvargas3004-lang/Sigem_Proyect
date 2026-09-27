package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoEmpleado {
    ACTIVO("Activo"),
    VACACIONES("De vacaciones"),
    LICENCIA("Con licencia / descanso"),
    CESADO("Cesado");

    private final String label;
}
