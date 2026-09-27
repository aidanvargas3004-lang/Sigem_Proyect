package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EstadoCampana {
    PLANIFICADA("Planificada"),
    ACTIVA("Activa"),
    FINALIZADA("Finalizada"),
    CANCELADA("Cancelada");

    private final String label;
}
