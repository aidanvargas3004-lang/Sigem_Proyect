package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoAsignacion {
    PROGRAMADA("Programada"),
    EN_CURSO("En curso"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String label;
}
