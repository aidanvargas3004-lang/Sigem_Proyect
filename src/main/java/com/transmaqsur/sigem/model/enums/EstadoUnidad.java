package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoUnidad {
    DISPONIBLE("Disponible"),
    ASIGNADA("Asignada"),
    EN_SERVICIO("En servicio"),
    EN_MANTENIMIENTO("En mantenimiento"),
    FUERA_SERVICIO("Fuera de servicio");

    private final String label;
}
