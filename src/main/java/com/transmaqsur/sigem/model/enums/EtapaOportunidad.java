package com.transmaqsur.sigem.model.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum EtapaOportunidad {
    PROSPECTO("Prospecto"),
    CALIFICADA("Calificada"),
    PROPUESTA("Propuesta"),
    NEGOCIACION("Negociación"),
    GANADA("Ganada"),
    PERDIDA("Perdida");

    private final String label;
}
